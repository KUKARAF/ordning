## ADDED Requirements
### Requirement: PDF Ticket Import
The system SHALL allow users to import PDF ticket files from device storage.

#### Scenario: Successful PDF import
- **WHEN** user selects a PDF file through file picker
- **THEN** the file is processed and stored locally
- **AND** QR codes are extracted and stored
- **AND** travel details are parsed into structured data

#### Scenario: Invalid PDF handling
- **WHEN** user selects a non-PDF or corrupted file
- **THEN** an appropriate error message is displayed
- **AND** the file is not processed

### Requirement: QR Code Extraction
The system SHALL extract and store all QR codes from imported PDF tickets.

#### Scenario: QR code detection
- **WHEN** a PDF contains QR codes
- **THEN** all QR codes are detected and extracted as images
- **AND** QR code positions and metadata are stored
- **AND** extracted QR codes are available for lockscreen display

#### Scenario: No QR codes found
- **WHEN** a PDF contains no QR codes
- **THEN** the ticket is still processed
- **AND** user is notified that no QR codes were found

### Requirement: Travel Data Parsing
The system SHALL parse travel information from PDF tickets including locations, times, and dates.

#### Scenario: Complete travel data extraction
- **WHEN** a PDF contains structured travel information
- **THEN** departure location is extracted
- **AND** destination location is extracted
- **AND** departure and arrival times are extracted
- **AND** timezone information is determined from departure location

#### Scenario: Partial data extraction
- **WHEN** PDF contains incomplete travel information
- **THEN** available data is extracted
- **AND** missing fields are marked for manual input
- **AND** user can complete missing information

### Requirement: Local Ticket Storage
The system SHALL store processed tickets locally in Room database.

#### Scenario: Ticket persistence
- **WHEN** a ticket is successfully processed
- **THEN** ticket data is stored in local database
- **AND** QR code images are stored in device storage
- **AND** ticket can be retrieved without network connection

#### Scenario: Ticket listing
- **WHEN** user views ticket list
- **THEN** all stored tickets are displayed
- **AND** each ticket shows key travel information
- **AND** tickets are sortable by date and destination