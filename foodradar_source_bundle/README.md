# Food Radar - Consolidated Source Code Bundle

This folder contains the complete, consolidated source code for the **Food Radar** delivery platform featuring real-time live order tracking, secure multi-method escrow payments, and synchronized multi-user accounts.

## Folder Directory Structure

```
foodradar_source_bundle/
├── java/
│   ├── MainActivity.kt                 # Main app container, role switching & navigation state
│   ├── model/
│   │   └── FoodModels.kt               # Domain data models, Enums, UserRole, OrderStatus, etc.
│   ├── data/
│   │   └── FoodDeliveryRepository.kt   # Central reactive StateFlow repository & real-time simulation
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt                # Brand palette & Material 3 semantic tokens
│       │   ├── Theme.kt                # Light/Dark ColorScheme & MaterialTheme setup
│       │   └── Type.kt                 # Typography definitions
│       ├── components/
│       │   └── RoleSelectorBar.kt      # Global switcher pill bar for Buyer, Seller, Courier, Admin
│       ├── buyer/
│       │   ├── BuyerHomeScreen.kt      # Restaurant list, search, food categories & hero banner
│       │   ├── RestaurantDetailScreen.kt # Restaurant menu, customization bottom sheet
│       │   ├── CartAndCheckoutScreen.kt# Cart summary, promo engine, card/wallet payment & 3D Secure OTP
│       │   └── LiveOrderTrackingScreen.kt # Interactive Canvas delivery radar map, live telemetry, PIN card
│       ├── seller/
│       │   └── SellerDashboardScreen.kt# Kitchen queue, order status actions, inventory & add dish modal
│       ├── courier/
│       │   └── CourierDashboardScreen.kt# Available delivery jobs, GPS simulator & PIN verification modal
│       └── admin/
│           └── AdminPanelScreen.kt     # GMV/commission KPIs, operations monitor & escrow transaction ledger
└── README.md                           # Directory guide
```
