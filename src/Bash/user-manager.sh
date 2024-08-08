#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
USER_STORE="$SCRIPT_DIR/../Storage/user-store.txt"
PATIENTS_STORE="$SCRIPT_DIR/../Storage/patients-store.txt"
INITIAL_ADMIN_EMAIL="admin@lpmt.com"
INITIAL_ADMIN_PASSWORD="admin123"

# Function to initialize user-store.txt if it doesn't exist and no admin exists
initialize_user_store() {
    new_uuid=$(uuidgen)
    if [ ! -f "$USER_STORE" ]; then
        touch "$USER_STORE"
        touch "$PATIENTS_STORE"
        
        initial_password_hash=$(echo -n "$INITIAL_ADMIN_PASSWORD" | sha256sum | awk '{print $1}')
        echo "$INITIAL_ADMIN_EMAIL,$new_uuid,$initial_password_hash,ADMIN,true,false" >> "$USER_STORE"
    else
        # Check if there is already an admin user
        if grep -q ",ADMIN,," "$USER_STORE"; then
            echo "Admin already exists. Initialization skipped."
            exit 0
        else
            initial_password_hash=$(echo -n "$INITIAL_ADMIN_PASSWORD" | sha256sum | awk '{print $1}')
            echo "$INITIAL_ADMIN_EMAIL,$new_uuid,$initial_password_hash,ADMIN,true,false" >> "$USER_STORE"
        fi
    fi
}

# Function to initiate user registration
initiate_registration() {
    uuid=$1
    email=$2

    # Check if the UUID exists and retrieve user details
    user_record=$(grep ",$uuid," "$USER_STORE")

    if [ -z "$user_record" ]; then
        echo "UUID not found."
        exit 0
    fi

    # Extract details from the user record
    access_type=$(echo "$user_record" | awk -F',' '{print $4}')
    is_logged_in=$(echo "$user_record" | awk -F',' '{print $6}')

    # Check if the user has admin access
    if [ "$access_type" != "ADMIN" ] || [ "$is_logged_in" != "true" ]; then
        echo "Access denied."
        exit 0
    fi

    # Validate email format
    if ! [[ "$email" =~ ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$ ]]; then
        echo "Invalid email format."
        exit 0
    fi

    # Check if email already exists
    if grep -q "^$email," "$USER_STORE"; then
        echo "Email already registered."
        exit 0
    fi

    # Generate a new UUID for the patient
    new_uuid=$(uuidgen)

    # Add the new patient to the user store
    echo "$email,$new_uuid,,PATIENT,false,false" >> "$USER_STORE"
    echo "Registration initiated. Use the following UUID to complete registration: $new_uuid"
}

get_life_expectancy() {
  local countryISO="$1"
  local life_expectancy

  life_expectancy=$(awk -F, -v iso="$countryISO" '
      NR > 1 {
        if ($5 == iso) {
          print $7
        }
      }
    ' $SCRIPT_DIR/../Storage/life-expectancy.csv)

  if [ -n "$life_expectancy" ]; then
    echo "$life_expectancy"
  else
    echo "Invalid country code"
  fi
}

calculate_age() {
  local birth_date="$1"
  local today=$(date +%Y-%m-%d)
  local birth_year=$(date -d "$birth_date" +%Y)
  local current_year=$(date -d "$today" +%Y)

  age=$(($current_year - $birth_year))
  echo $age
}

complete_registration() {
  # Check if the user-store file is empty
  if [ ! -s "$USER_STORE" ]; then
    echo "User was not initiated"
    return 0
  fi

  if [ $# -ne 10 ]; then
    echo "Usage: $0 complete-registration <uuid> <firstName> <lastName> <dateOfBirth> <hasHIV> <diagnosisDate> <onART> <artStartDate> <countryISO> <password>"
    exit 0
  fi

  uuid=$1
  firstName=$2
  lastName=$3
  dateOfBirth=$4
  hasHIV=$5
  diagnosisDate=$6
  onART=$7
  artStartDate=$8
  countryISO=$9
  password=${10}

  # Check if the UUID is already used
  if ! grep -q ",$uuid," "$USER_STORE"; then
      echo "User was not initiated"
      exit 0
  fi

  if grep -q ",$uuid,,PATIENT,true," "$USER_STORE"; then
      echo "Registration already completed for this UUID."
      exit 0
  fi

  password_hash=$(echo -n "$password" | sha256sum | awk '{print $1}')

  # Create a temporary file
  temp_file=$(mktemp)

  # Get the age
  current_age=$(calculate_age "$dateOfBirth")

  # Get the life expectancy for the country
  country_lifespan=$(get_life_expectancy "$countryISO")

  if [ "$country_lifespan" = "Invalid country code" ]; then
    echo "Failed to complete registration due to invalid country code."
    exit 1
  fi

  # Calculate the remaining lifespan
  remaining_years=$(echo "$country_lifespan - $current_age" | bc)
  years_delayed=$(($(date -d "$artStartDate" +%Y) - $(date -d "$diagnosisDate" +%Y)))

  # Adjust remaining lifespan
  for ((i = 0; i <= $years_delayed; i++)); do
      remaining_years=$(echo "$remaining_years * 0.9" | bc | awk '{print int($1)}')
  done

  # Calculate demise date
  demise_date=$(date -d "+$remaining_years years" +"%Y-%m-%d")

  # Update the user record and add patient details
  awk -v uuid="$uuid" -v password_hash="$password_hash" \
    'BEGIN { FS=","; OFS="," }
     {
       if ($2 == uuid) {
         $3 = password_hash
         $5 = "true"
       }
       print
     }' "$USER_STORE" > "$temp_file" && mv "$temp_file" "$USER_STORE"

  echo "$uuid,$firstName,$lastName,$dateOfBirth,$hasHIV,$diagnosisDate,$onART,$artStartDate,$countryISO,$remaining_years,$demise_date" >> "$PATIENTS_STORE"
  echo "Registration completed for user with UUID: $uuid"
  echo "Expected lifespan: $remaining_years years"
  echo "Expected demise date: $demise_date"
}

logout(){
  email=$1

  temp_file=$(mktemp)

  awk -v email="$email" \
    'BEGIN { FS=OFS="," }
    {
      if ($1 == email) {
        $6="false"
      }
      print
    }' "$USER_STORE" > "$temp_file" && mv "$temp_file" "$USER_STORE"
  echo "User logged out successfully"
}

login() {
  email=$1
  password=$2
  password_hash=$(echo -n "$password" | sha256sum | awk '{print $1}')

  # Check if the user-store file exists and initialize if not
  if [ ! -f "$USER_STORE" ]; then
    initialize_user_store
  fi

  # Check if the user-store file is empty
  if [ ! -s "$USER_STORE" ]; then
    echo "User store is empty. Please register an admin first."
    return 0
  fi

  temp_file=$(mktemp)
  login_successful=false

  while IFS=, read -r stored_email stored_uuid stored_password_hash access_type is_registered is_logged_in
  do
    if [[ "$stored_email" == "$email" ]]; then
      if [[ "$stored_password_hash" == "$password_hash" ]]; then
        if [[ "$is_registered" == "true" ]]; then
          # Set is_logged_in to true
          awk -v uuid="$stored_uuid" -v password_hash="$password_hash" \
            'BEGIN { FS=OFS="," }
            {
              if ($2 == uuid && $3 == password_hash) {
                $6="true"
              }
              print
            }' "$USER_STORE" > "$temp_file" && mv "$temp_file" "$USER_STORE"
          echo "$access_type,$stored_uuid"
          login_successful=true
        else
          echo "User should first complete the registration"
          login_successful=true
        fi
      else
        echo "Login failed. Incorrect username or password."
        login_successful=true
      fi
      break
    fi
  done < "$USER_STORE"

  if [ "$login_successful" = false ]; then
    echo "Login failed. Email not found."
  fi

  return 0
}

view_profile() {
  uuid_code=$1

  # Check if the UUID exists and retrieve user details
  user_record=$(grep ",$uuid_code," "$USER_STORE")

  if [ -z "$user_record" ]; then
      echo "UUID not found."
      exit 0
  fi

  # Extract details from the user record
  is_logged_in=$(echo "$user_record" | awk -F',' '{print $6}')

  # Check if the user has admin access
  if [ "$is_logged_in" != "true" ]; then
      echo "Access denied."
      exit 0
  fi

  while IFS=, read -r stored_uuid firstname lastname dateOfBirth isHivPositive dateOfInfection onArtDrugs startARTDate country lifeExpectancy demiseDate
  do
    if [[ "$stored_uuid" == "$uuid_code" ]]; then
      echo "First Name: $firstname"
      echo "Last Name: $lastname"
      echo "Date of Birth: $dateOfBirth"
      echo "Is HIV Positive: $isHivPositive"
      echo "Date of Infection: $dateOfInfection"
      echo "On ART Drugs: $onArtDrugs"
      echo "Date of start ART: $startARTDate"
      echo "Country: $country"
      echo "Life Expectancy: $lifeExpectancy"
      echo "Demise Date: $demiseDate"
      return 0
    fi
  done < "$PATIENTS_STORE"

  echo "Profile not found for the given UUID."
  return 0
}

get_all_users() {
  if [ "$1" != "ADMIN" ]; then
      echo "Access denied"
      exit 0
  fi

  users=()

  while IFS=, read -r stored_uuid firstname lastname dateOfBirth isHivPositive dateOfInfection onArtDrugs startARTDate country lifeExpectancy demiseDate
  do
    user="$stored_uuid,$firstname,$lastname,$dateOfBirth,$isHivPositive,$dateOfInfection,$onArtDrugs,$startARTDate,$country,$lifeExpectancy,$demiseDate"
    users+=("$user")
  done < "$PATIENTS_STORE"

  for user in "${users[@]}"; do
    echo "$user"
  done
}

validate_uuid() {
  if [ ! -f "$USER_STORE" ]; then
    initialize_user_store
  fi

    uuid=$1

    while IFS=, read -r stored_email stored_uuid stored_password_hash access_type is_registered is_logged_in
    do
        if [[ "$stored_uuid" == "$uuid" && "$is_registered" == "false" ]]; then
            echo "$stored_email"
            return 0
        fi
    done < "$USER_STORE"

    echo "Invalid or already registered UUID."
    return 0
}

validate_date() {
  # Check if the date is in YYYY-MM-DD format
  date -d "$1" &>/dev/null
}

modify_patient_profile() {
  if [ $# -ne 9 ]; then
    echo "Usage: $0 modify-patient-profile <uuid> <firstName> <lastName> <dateOfBirth> <hasHIV> <diagnosisDate> <onART> <artStartDate> <countryISO>"
    exit 1
  fi

  uuid=$1

  # Check if the UUID exists and retrieve user details
  user_record=$(grep ",$uuid," "$USER_STORE")

  if [ -z "$user_record" ]; then
      echo "UUID not found."
      exit 0
  fi

  # Extract details from the user record
  is_logged_in=$(echo "$user_record" | awk -F',' '{print $6}')

  # Check if the user has admin access
  if [ "$is_logged_in" != "true" ]; then
      echo "Access denied."
      exit 0
  fi

  firstName=$2
  lastName=$3
  dateOfBirth=$4
  hasHIV=$5
  diagnosisDate=$6
  onART=$7
  artStartDate=$8
  countryISO=$9

  # Validate the date format
  if ! validate_date "$dateOfBirth"; then
    echo "Invalid date format for Date of Birth. Use YYYY-MM-DD."
    exit 1
  fi
  
  if [ "$hasHIV" == "true" ]; then
    if ! validate_date "$diagnosisDate"; then
      echo "Invalid date format for Diagnosis Date. Use YYYY-MM-DD."
      exit 1
    fi
    
    if [[ "$diagnosisDate" < "$dateOfBirth" ]]; then
      echo "Diagnosis Date cannot be before Date of Birth."
      exit 1
    fi
  fi

  temp_file=$(mktemp)
  uuid_found=false

  while IFS=, read -r stored_uuid stored_firstName stored_lastName stored_dateOfBirth stored_hasHIV stored_diagnosisDate stored_onART stored_artStartDate stored_countryISO stored_remaining_years stored_demise_date
  do
    if [[ "$stored_uuid" == "$uuid" ]]; then
      # Update fields only if new values are provided
      new_firstName=${firstName:-$stored_firstName}
      new_lastName=${lastName:-$stored_lastName}
      new_dateOfBirth=${dateOfBirth:-$stored_dateOfBirth}
      new_hasHIV=${hasHIV:-$stored_hasHIV}
      new_diagnosisDate=${diagnosisDate:-$stored_diagnosisDate}
      new_onART=${onART:-$stored_onART}
      new_artStartDate=${artStartDate:-$stored_artStartDate}
      new_countryISO=${countryISO:-$stored_countryISO}

      # Calculate the new age and remaining lifespan if required
      current_age=$(calculate_age "$new_dateOfBirth")
      country_lifespan=$(get_life_expectancy "$new_countryISO")

      if [ "$country_lifespan" = "Invalid country code" ]; then
        echo "Failed to update profile due to invalid country code."
        rm "$temp_file"
        exit 1
      fi

      # If the patient has HIV, calculate the remaining lifespan and demise date
      if [ "$new_hasHIV" == "true" ]; then
        remaining_years=$(echo "$country_lifespan - $current_age" | bc)
        years_delayed=$(($(date -d "$new_artStartDate" +%Y) - $(date -d "$new_diagnosisDate" +%Y)))

        # Adjust remaining lifespan
        for ((i = 0; i <= $years_delayed; i++)); do
          remaining_years=$(echo "$remaining_years * 0.9" | bc)
        done

        # Round up to the next full year
        rounded_years=$(echo "($remaining_years + 0.999999999)" | bc | awk '{print int($1)}')

        # Calculate demise date
        demise_date=$(date -d "+$rounded_years years" +"%Y-%m-%d")
      else
        # If the patient does not have HIV, assume normal life expectancy
        rounded_years=$(echo "$country_lifespan - $current_age" | bc | awk '{print int($1)}')
        demise_date=$(date -d "+$rounded_years years" +"%Y-%m-%d")
      fi

      echo "$uuid,$new_firstName,$new_lastName,$new_dateOfBirth,$new_hasHIV,$new_diagnosisDate,$new_onART,$new_artStartDate,$new_countryISO,$rounded_years,$demise_date" >> "$temp_file"
      uuid_found=true
    else
      echo "$stored_uuid,$stored_firstName,$stored_lastName,$stored_dateOfBirth,$stored_hasHIV,$stored_diagnosisDate,$stored_onART,$stored_artStartDate,$stored_countryISO,$stored_remaining_years,$stored_demise_date" >> "$temp_file"
    fi
  done < "$PATIENTS_STORE"

  if [ "$uuid_found" = true ]; then
    mv "$temp_file" "$PATIENTS_STORE"
    echo "Patient profile updated successfully."
  else
    echo "Profile not found for the given UUID."
    rm "$temp_file"
    exit 1
  fi
}

case $1 in
  "initialize-user-store")
    initialize_user_store
    ;;
  "get-life-expectancy")
    get_life_expectancy $2
    ;;
  "validate-uuid")
    if [ $# -ne 2 ]; then
        echo "Usage: $0 validate-uuid <uuid>"
        exit 1
    fi
    validate_uuid $2
    ;;
  "get-all-users")
    get_all_users
    ;;
  "initiate-registration")
    if [ $# -ne 3 ]; then
      echo "Usage: $0 initiate-registration <UUID_code> <email>"
      exit 1
    fi
    initiate_registration $2 $3
    ;;
  "complete-registration")
    shift
    complete_registration "$@"
    ;;
  "view-profile")
    if [ $# -ne 2 ]; then
      echo "Usage: $0 view-profile <UUID_code>"
      exit 1
    fi
    view_profile $2
    ;;
  "login")
    if [ $# -ne 3 ]; then
      echo "Usage: $0 login <email> <password>"
      exit 1
    fi
    login $2 $3
    ;;
  "logout")
    if [ $# -ne 2 ]; then
      echo "Usage: $0 logout <email>"
      exit 1
    fi
    logout $2
    ;;
  "modify-patient-profile")
    shift
    modify_patient_profile "$@"
    ;;
  *)
    echo "Unknown command: $1"
    echo "Usage: $0 <initialize-user-store|initiate-registration|complete-registration|view-profile|login|modify-patient-profile> [<args>]"
    exit 1
    ;;
esac