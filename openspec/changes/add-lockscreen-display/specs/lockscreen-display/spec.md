## ADDED Requirements
### Requirement: Lockscreen QR Display
The system SHALL display QR codes on lockscreen 1 hour before and after travel times.

#### Scenario: Pre-travel display
- **WHEN** current time is 1 hour before departure
- **THEN** notification is created with lockscreen display
- **AND** QR codes are shown at full screen width
- **AND** original ticket is displayed below QR codes
- **AND** display persists until departure time

#### Scenario: Post-travel display
- **WHEN** current time is within 1 hour after arrival
- **THEN** QR codes are displayed on lockscreen
- **AND** full-width QR code layout is maintained
- **AND** original ticket remains accessible
- **AND** display ends after 1 hour post-arrival

### Requirement: Full-Width QR Display
The system SHALL display QR codes at full screen width for easy scanning.

#### Scenario: QR code scaling
- **WHEN** QR codes are displayed on lockscreen
- **THEN** QR codes occupy full width of screen
- **AND** aspect ratio is maintained for readability
- **AND** multiple QR codes are displayed sequentially
- **AND** each QR code is clearly visible and scannable

#### Scenario: Original ticket display
- **WHEN** QR codes are displayed
- **THEN** original PDF ticket is shown below
- **AND** ticket can be scrolled/zoomed
- **AND** full ticket content remains accessible
- **AND** layout matches pdfwallet-android reference

### Requirement: Time-Based Scheduling
The system SHALL schedule lockscreen displays based on travel times.

#### Scenario: Pre-travel scheduling
- **WHEN** ticket is processed with travel times
- **THEN** display is scheduled for 1 hour before departure
- **AND** notification is created at scheduled time
- **AND** system handles device reboots/resets
- **AND** scheduling survives app restarts

#### Scenario: Post-travel scheduling
- **WHEN** ticket has arrival time
- **THEN** display is scheduled for arrival time
- **AND** display continues for 1 hour after arrival
- **AND** cleanup occurs automatically after display period
- **AND** resources are properly released

### Requirement: Notification Management
The system SHALL manage notifications for lockscreen QR display.

#### Scenario: Notification creation
- **WHEN** lockscreen display is triggered
- **THEN** persistent notification is created
- **AND** notification shows travel details
- **AND** notification provides quick access to QR display
- **AND** notification cannot be dismissed accidentally

#### Scenario: Notification lifecycle
- **WHEN** display period ends
- **THEN** notification is automatically dismissed
- **AND** lockscreen display is removed
- **AND** system resources are cleaned up
- **AND** user can manually dismiss if needed

### Requirement: Permission Handling
The system SHALL request and manage necessary permissions for lockscreen display.

#### Scenario: Permission requests
- **WHEN** user enables lockscreen display feature
- **THEN** notification permission is requested
- **AND** overlay permission is requested if needed
- **AND** purpose of each permission is explained
- **AND** feature is disabled until permissions granted

#### Scenario: Permission monitoring
- **WHEN** permissions are revoked
- **THEN** user is notified of lost functionality
- **AND** feature is gracefully disabled
- **AND** re-authentication flow is available
- **AND** other features remain unaffected