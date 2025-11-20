# Change: Add Lockscreen QR Code Display

## Why
Display ticket QR codes prominently on lockscreen 1 hour before and after travel times for easy access during transit, similar to pdfwallet-android functionality.

## What Changes
- Add lockscreen integration for QR code display
- Implement time-based notification system
- Create full-width QR code display overlay
- Add original ticket display below QR codes
- Implement notification scheduling service
- **BREAKING**: Requires notification and overlay permissions

## Impact
- Affected specs: lockscreen-display (new)
- Affected code: Background services, notification system, UI overlays, permissions