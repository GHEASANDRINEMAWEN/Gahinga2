#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
USER_STORE="$SCRIPT_DIR/../Storage/user-store.txt"
PATIENTS_STORE="$SCRIPT_DIR/../Storage/patients-store.txt"
INITIAL_ADMIN_EMAIL="admin@example.com"
INITIAL_ADMIN_PASSWORD="admin123"

# Function to initialize user-store.txt if it doesn't exist and no admin exists
initialize_user_store() {
  if [ ! -f "$USER_STORE" ]; then
    touch "$USER_STORE"
    touch "$PATIENTS_STORE"
    
    initial_password_hash=$(echo -n "$INITIAL_ADMIN_PASSWORD" | sha256sum | awk '{print $1}')
    echo "$INITIAL_ADMIN_EMAIL,,$initial_password_hash,ADMIN,true" >> "$USER_STORE"
    echo "Initialized user store with the initial admin user."
  else
    # Check if there is already an admin user
    if grep -q ",,,$initial_password_hash,ADMIN,true" "$USER_STORE"; then
      echo "Admin already exists. Initialization skipped."
      exit 0
    else
      initial_password_hash=$(echo -n "$INITIAL_ADMIN_PASSWORD" | sha256sum | awk '{print $1}')
      echo "$INITIAL_ADMIN_EMAIL,,$initial_password_hash,ADMIN,true" >> "$USER_STORE"
      echo "Initialized user store with the initial admin user."
    fi
  fi
}

# Function to initiate user registration
initiate_registration() {
  if [ "$1" != "ADMIN" ]; then
    echo "You have no access to initiate registration"
    exit 0
  fi
  email=$2

  if ! [[ "$email" =~ ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$ ]]; then
    echo "Invalid email format."
    exit 0
  fi

  uuid=$(uuidgen)
  echo "$email,$uuid,,PATIENT,false" >> "$USER_STORE"
  echo "Registration initiated. Use the following UUID to complete registration: $uuid"
}


complete_registration() {
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

  password_hash=$(echo -n "$password" | sha256sum | awk '{print $1}')

  # Check if the UUID is already used
  if ! grep -q ",$uuid,," "$USER_STORE"; then
    echo "User was not initiated"
    exit 0
  fi

  # Create a temporary file
  temp_file=$(mktemp)

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

  echo "$uuid,$firstName,$lastName,$dateOfBirth,$hasHIV,$diagnosisDate,$onART,$artStartDate,$countryISO,," >> "$PATIENTS_STORE"
  echo "Registration completed for user with UUID: $uuid"
}

login() {
  email=$1
  password=$2
  password_hash=$(echo -n "$password" | sha256sum | awk '{print $1}')

  while IFS=, read -r stored_email stored_uuid stored_password_hash access_type is_registered
  do
    if [[ "$stored_email" == "$email" ]]; then
      if [[ "$stored_password_hash" == "$password_hash" ]]; then
        if [[ "$is_registered" == "true" ]]; then
          echo "$access_type,$stored_uuid"
        else
          echo "User should first complete the registration"
        fi
        return 0
      else
        echo "Login failed. Incorrect password."
        return 0
      fi
    fi
  done < "$USER_STORE"

  echo "Login failed. Email not found."
  return 0
}

view_profile() {
  uuid_code=$1

  while IFS=, read -r stored_uuid firstname lastname dateOfBirth isHivPositive dateOfInfection onArtDrugs startARTDate country demiseDate lifeExpectancy
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
      echo "Demise Date: $demiseDate"
      echo "Life Expectancy: $lifeExpectancy"
      return 0
    fi
  done < "$PATIENTS_STORE"

  echo "Profile not found for the given UUID."
  return 0
}

validate_date() {
  # Check if the date is in YYYY-MM-DD format
  date -d "$1" &>/dev/null
}

modify_patient_profile() {
  echo "Arguments received: $@"

  if [ $# -ne 9 ]; then
    echo "Usage: $0 modify-patient-profile <uuid> <firstName> <lastName> <dateOfBirth> <hasHIV> <diagnosisDate> <onART> <artStartDate> <countryISO>"
    exit 1
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

  while IFS=, read -r stored_uuid stored_firstName stored_lastName stored_dateOfBirth stored_hasHIV stored_diagnosisDate stored_onART stored_artStartDate stored_countryISO
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

      echo "$uuid,$new_firstName,$new_lastName,$new_dateOfBirth,$new_hasHIV,$new_diagnosisDate,$new_onART,$new_artStartDate,$new_countryISO" >> "$temp_file"
      uuid_found=true
    else
      echo "$stored_uuid,$stored_firstName,$stored_lastName,$stored_dateOfBirth,$stored_hasHIV,$stored_diagnosisDate,$stored_onART,$stored_artStartDate,$stored_countryISO" >> "$temp_file"
    fi
  done < "$PATIENTS_STORE"

  if [ "$uuid_found" = true ]; then
    mv "$temp_file" "$PATIENTS_STORE"
    echo "Patient profile updated successfully."
  else
    echo "Profile not found for the given UUID."
    echo "Contents of $PATIENTS_STORE:"
    cat "$PATIENTS_STORE"  # Print file contents for debugging
    rm "$temp_file"
    exit 1
  fi
}

case $1 in
  "initialize-user-store")
    initialize_user_store
    ;;
  "initiate-registration")
    if [ $# -ne 3 ]; then
      echo "Usage: $0 initiate-registration <UserRole> <email>"
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