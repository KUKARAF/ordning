# Change: Add Calendar Event Creation

## Why
Enable automatic creation of calendar events from parsed ticket data with proper travel details, timezone handling, and Google Calendar synchronization.

## What Changes
- Add calendar event creation from ticket data
- Implement timezone detection based on departure location
- Add Google Calendar API integration
- Create ICS file generation capability
- Add calendar selection and management UI
- **BREAKING**: Requires Google Calendar permissions

## Impact
- Affected specs: calendar-integration (new)
- Affected code: Ticket processing flow, Google services integration, UI components