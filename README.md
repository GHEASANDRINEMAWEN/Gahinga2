# Life Prognosis Management Tool
Welcome to the Life Prognosis Management Tool. This application is designed to manage patient information, particularly in the context of managing HIV-positive patients. It allows users to register, update, and retrieve patient information securely and efficiently.


## Table of Contents
* Project Overview
* Features
* Project Structure
* Prerequisites
* Installation
* Running the Application
* Contributing

## Project Overview
The Life Prognosis Management Tool is a command-line application that allows users to manage patient information. The system is designed with security and efficiency in mind and provides different levels of access to users based on their roles.


### Features
* `User Authentication:` Secure login functionality to ensure that only authorized users can access patient data.
* `Patient Management:` Register new patients, update patient information, and manage HIV-related data.
* `Data Validation:` Ensures that the data entered is accurate and consistent.
* `Country Information Integration:` Validates and stores patient country information based on a CSV file.
* `Bash Script Automation:` Automates various tasks using Bash scripts, including user management and patient data handling.
### Project Structure
The project consists of the following key components:

* `src/:` Contains the source code for the application, including Java and Bash scripts.
* `src/Bash/:` Contains the Bash scripts for user and patient management.
* `src/Storage/:` Includes data files such as user-store.txt and patients-store.txt to store user and patient information.
* `src/Storage/life-expectancy.csv:` A CSV file containing country codes, names, and life expectancy data.
* `README.md:` This file, providing information on how to set up and use the project.
## Prerequisites
Before you can run the application, ensure that you have the following installed on your machine:

* Java Development Kit (JDK) - Version 8 or higher
* Bash - Installed on Unix-like operating systems (Linux, macOS)
* Git - For cloning the repository
* Text Editor/IDE - To view and edit the source code if necessary
## Installation
Follow these steps to clone and set up the project on your machine:

### 1. Clone the Repository:

```
git clone https://github.com/GHEASANDRINEMAWEN/Gahinga2.git
```

### 2. Navigate to the Project Directory:

```
cd Gahinga2
```

### 3. Set Up the Data Files:

Ensure that the data/ directory contains the necessary files (user-store.txt, patients-store.txt).

### 4. Verify Java Installation:

Make sure Java is properly installed and accessible in your environment. You can check this by running:

```
java -version
```

### 5. Verify Bash Installation:

Ensure Bash is installed and accessible. You can check this by running:

```
bash --version
```

## Notes
* The application will prompt you to log in on startup.
* Based on your access level, you will be given options to manage users, patients, or view data.
## Contributing
If you'd like to contribute to this project, please follow these steps:

* Fork the repository.
* Create a new branch (git checkout -b feature-branch).
* Make your changes.
* Commit your changes (git commit -m 'Add some feature').
* Push to the branch (git push origin feature-branch).
* Open a Pull Request.
