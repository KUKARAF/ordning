# Change: Add PDF reading and calendar event creation

## Why
Users need to extract travel information from PDF tickets and automatically create calendar events with proper location, time, and timezone data.

## What Changes
- Add PDF parsing capability to extract travel destinations, times, and locations
- Create calendar event generation with destination as title and origin as location
- Implement timezone detection based on starting location
- Generate ICS files for calendar compatibility
- Store events locally before external sync

## Impact
- Affected specs: pdf-processing, calendar-management
- Affected code: PDF parser, calendar service, timezone detection
- **BREAKING**: New core data models for travel events