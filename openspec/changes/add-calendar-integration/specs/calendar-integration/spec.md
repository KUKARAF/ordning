## ADDED Requirements
### Requirement: Calendar Event Creation
The system SHALL create calendar events from processed ticket data with proper travel details.

#### Scenario: Event creation from ticket
- **WHEN** a processed ticket is selected for calendar creation
- **THEN** event title is set to destination (e.g., ">> warszawa centralna")
- **AND** event location is set to departure point (e.g., "Olsztyn Głowny")
- **AND** event start/end times match travel times
- **AND** event timezone matches departure location timezone

#### Scenario: Multiple calendar support
- **WHEN** user has multiple Google Calendars
- **THEN** user can select target calendar(s)
- **AND** events can be created in multiple calendars
- **AND** calendar selection is saved in settings

### Requirement: Timezone Detection
The system SHALL automatically detect and apply the correct timezone based on departure location.

#### Scenario: Automatic timezone detection
- **WHEN** ticket contains departure location
- **THEN** system determines timezone for that location
- **AND** calendar event uses detected timezone
- **AND** times are correctly converted to local timezone

#### Scenario: Manual timezone override
- **WHEN** automatic detection fails or is incorrect
- **THEN** user can manually select timezone
- **AND** manual selection is saved for similar locations
- **AND** event is updated with correct timezone

### Requirement: ICS File Generation
The system SHALL generate ICS files for calendar events that can be shared or imported.

#### Scenario: ICS file creation
- **WHEN** user chooses to export event as ICS
- **THEN** ICS file is generated with all event details
- **AND** file includes proper timezone information
- **AND** file can be shared via standard Android sharing

#### Scenario: ICS file compatibility
- **WHEN** ICS file is imported to other calendar apps
- **THEN** all event details are preserved
- **AND** timezone information is correctly interpreted
- **AND** event appears correctly in target calendar

### Requirement: Google Calendar Integration
The system SHALL integrate with Google Calendar API for event creation and management.

#### Scenario: Google Calendar authentication
- **WHEN** user enables Google Calendar sync
- **THEN** OAuth authentication flow is initiated
- **AND** appropriate permissions are requested
- **AND** authentication tokens are securely stored

#### Scenario: Event synchronization
- **WHEN** events are created locally
- **THEN** they are synchronized to selected Google Calendars
- **AND** sync conflicts are handled gracefully
- **AND** sync status is visible to user

### Requirement: Calendar Settings Management
The system SHALL provide settings for calendar integration preferences.

#### Scenario: Default calendar configuration
- **WHEN** user accesses calendar settings
- **THEN** default calendar(s) can be selected
- **AND** auto-creation preferences can be configured
- **AND** sync frequency can be adjusted

#### Scenario: Calendar permission management
- **WHEN** calendar permissions are revoked
- **THEN** user is notified of lost functionality
- **AND** re-authentication flow is available
- **AND** local events remain accessible