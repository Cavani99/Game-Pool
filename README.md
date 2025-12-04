# Game Pool

A web application used as a video-game shop, where you can wishlist / buy games, add other users as friends and manage
funds.
It contains an Admin and User side.The Admin manages games, game companies and game Categories, he can also ban user
accounts.

## Application Tech Stack

- **Java:** 17
- **Spring Boot:** 3.4.0
- **Build Tool:** Maven
- **Database:** MySQL, H2
- **Backend Framework:** Spring MVC
- **Frontend:** Thymeleaf
- **ORM:** Spring Data JPA
- **Security:** Spring Security
- **Logging:** SLF4J / Logback
- **Microservices:** Open Feign Client
- **Testing:** H2Database, Spring Test, Jacoco (For measuring Tests Line coverage)
- **Integration With:** [Notification System (Creating and receiving notifications)](https://github.com/Cavani99/Notification-System)

## Administrator Functions

### 1. Modify Game Companies

**Keeps information about game-making companies, that a game can use as a foreign key**

- **Fields:**
    - Name
- **Actions:**
    - Create
    - Edit
    - Delete

### 2. Modify Game Categories

**Keeps information about game genres, identical to the categories**

- **Fields:**
    - Name
- **Actions:**
    - Create
    - Edit
    - Delete

### 3. Modify Games

**Used for saving and modifying games, which will be sold in the website.**

- **Fields:**
    - Title - Name of the Game
    - Description
    - Category - relationship with Category Model , used for filtering in the application games list
    - Company - relationship with Company Model , used for filtering in the application games list
    - Image - Saved locally
    - Price
    - Discount - relationship with Discount Model , used for making promotions for the game
    - Owners - relationship with User Model, for the users that have bought the game
    - usersWishlisted - relationship with User Model, for the users that have just wishlisted the game, but do not own
      it yet
- **Actions:**
    - Create
    - Edit
    - Delete
    - Add Discount - If a new discount is created for the game, and Notification System API is working,
      notifications are sent to the users that have wishlisted the game.

### 4. Modify Game Discounts

**Keeping discounts about a game, with a period when it begins and when it ends and amount discounted.
Discount is active, if _End Date_ is in the future,_Start Date_ is in the past or _Both Dates_ are set**

- **Fields:**
    - Amount - How much is the game discounted. Can not be more than 100, if the type is percent. Can not be more than
      the game price
    - Type - Type of discount, either FIXED or PERCENT. FIXED removes its amount from the game price directly,
      PERCENT is percentage of the game price
    - startDate - starting date of the discount
    - EndDate - end of the discount
- **Actions(In Games List):**
    - Create
    - Edit
    - Delete
    - Add Discount (If a new discount is created for the game, and Notification System API is working,
      notifications are sent to the users that have wishlisted the game).

### 5. See Users

**See all the registered users, with role _User_**

- **Fields:**
    - Username - Must be unique, used for login.
    - Email - Must be unique, currently informational.
    - Password - used for logging in.
    - Avatar - saved locally. An image used from the user
    - Role - It can User or Admin. Admin is used to modify data for the website, and User is used to interact with the
      website.
    - isBanned - holds a value, if the profile is currently active.
    - Games - List of Games, that the user has bought.
    - wishlistGames - List of Games, that the user has wishlisted, but not yet owns.
    - Balance - available funds of the _User_, that are used to buy games, or sent to friends.
    - Friends - other Users, that have been added as friends.
- **Actions:**
    - See - see the information of the user (Username, Avatar, Email and Balance)
    - Ban/Unban - ban or unban user, to either stop him from using the website, or allow him to do it again.

### 6. See/Edit Your Profile

**See your profile. Homepage for the Admin Side**

- **Actions:**
    - Edit - you can change the avatar and username of the currently logged-in user

### 6. See Transactions

**See the transactions made, from the _Users_**

- **Fields:**
    - Description - Contains information for the purpose of the transaction.
    - Sender - The User that has made the transaction, **None**, if its self-transaction (Receiver = Sender).
    - Receiver - The User that is on the receiving end of the transaction. **None**, if it`s buying a game.
    - Amount - the amount of funds spent.
    - createdOn (Date) - when the transaction was created.

## User Functions

### 1. See/Edit Profile

**See your profile. You can change your picture (avatar), username and password. Be aware that, if you change
your username, you need to log in with the new username. You can also go to your wallet page, where you can modify your
funds, and
see your owned games. There is also a bell in the upper right corner, that show you how many notifications do you have**

- **Actions:**
    - Edit Profile - you can change the avatar and username
    - Change Password - Both fields need to match
    - See Wallet - Redirects you to [Wallet](https://github.com/Cavani99/Game-Pool?tab=readme-ov-file#see-wallet) view
    - See Owned Games - Shows you the games you have already bought

### 2. Look at Games List, Buy or Wishlist Game

**Look at the Game`s Shop, where you can see each Game details and buy it, or add it to wishlist. Game can also be
filtered
by Category and Company**

- **Actions:**
    - Filter - you can filter games, but category or company, to see specific games.
    - See Game Details - see the game in detail, with description. You can add it to wishlist from here, or buy it,
      if you have the funds.

### 3. Friends List, Add or Remove Friend, Send Friend Invite

**See information about your friends`s profiles and look at their owned games. You can search for friends by username
and sent a friend request**

- **Actions:**
    - See Friend Profile - see your friend`s profile, and his owned games.
    - Remove Friend - remove friend from your friends list
    - Search for Friend By Username - you can see his profile also
    - Send Friend Request - send to another user a friend request. This is made using the **_Notification System_** API,
      and works, if the API is currently running. The other _User_ receives a notification.

### 4. Wish List, See your wishlisted games

**Similar to Game List, but it only contains games you have added to wishlist**

- **Actions:**
    - Filter - by Category and Company
    - Remove - remove the Game from Wishlist
    - See More - see Game details, where you can also remove it from wishlist or buy it

### 5. See Wallet

**Add funds to your account, send funds to friends, and see your previously received or sent transactions**

- **Actions:**
    - Add Funds - add funds to your account
    - Send Funds - send funds to a friend

### 5. See Notifications

**See your notifications, that you receive
from [Notification System (Creating and receiving notifications)](https://github.com/Cavani99/Notification-System).
Notification can be Informational or a Request with a link to complete. Currently, there are notifications for game
discounts and
friend invitations.**

- **Actions:**
    - Remove Notification - remove notification
    - See Notification - see notification details

## Settings and description

### Roles

- **USER** - has access to the website
- **ADMIN** - has access to the admin panel, where he can manage the data

### Endpoints

- **Open endpoints** - (`/`, `/register`, `/login`, `/error`, `/.well-known/**`, `"/css/**"`, `/js/**`)
- **Authenticated endpoints** - Logged users endpoints(`/dashboard/**`)
- **Authorized endpoints** - Logged admins endpoints(`/admin/**`)

### Security Info

- CSRF is used
- BCryptPasswordEncoder used for passwords
- Users can edit their username, avatar and password, Admins can modify only their username and avatar
- Administrators can ban/unban other users

## Databases

- **Database:** MySQL
- **ORM:** Spring Data JPA
- **Primary Keys:** UUID за всички entities
- **Integration Tests** H2Database
- **Entity Relationships:**
    - Game ↔ Category (Many-to-One)
    - Game ↔ Company (Many-to-One)
    - Game ↔ User (Many-to-Many)
    - Game ↔ Discount (One-to-One)
    - Transaction ↔ User (Many-to-One)
    - User ↔ User (Many-to-Many) (For Friends)

### Entities

1. **User** - the users that will interact with the website
2. **Company** - Companies of the Games
3. **Category** - Categories of the Games
4. **Games** - The Games that will be sold
5. **Discount** - discount for the game
6. **Transaction** - saving the funds movements

## Scheduling & Caching

### Scheduled Jobs

- **NotificationController.sentDiscountNotifications()** - Runs every 1 hour, creates notifications for newly discounted games and sends 
them to users that have wishlisted the game
- **NotificationController.removeExpiredGameDiscountsNotifications()** - Runs every hour after previous run completes, removes old discount notifications

### Caching

- **Cacheable:** Used in GameService.findAll()
- **CacheEvict:** Used when a game is created, edited or deleted

## Microservice Application

**Uses `notification-system` for creating notifications and sending them to users.
FeignClient is used for communication with the microservice**

- **URL:** `http://localhost:8081/notification/v1`
- **DTOs:** - Used to access the data from the microservice
  - CreateNotificationRequest - filling the data for creating notifications in the other application
  - CreateUserRequest - sending the user`s information, so it can manage its notifications right, and can decide which user receives the notification
  - NotificationObject - used for showing the information, Takes NotificationResponse, so it can get the username
  - NotificationResponse - the raw notification response

##  Logging

**`logback-spring.xml` is used to change the log`s design and color, and to save errors in files.
Files are saved for 35 days.**


- logger.info - when opening modification views (forms) and when a function is completed successfully
- logger.error - when an operation has failed in completing. Also saved in `logs` folder, sorted by day. File
`error.log` is used for the current day.

## Frontend

**Thymeleaf used for dynamic rendering, and sometimes AJAX for filters and searching**

##  Error Handling

Приложението включва глобална обработка на грешки:

- **GlobalExceptionHandler** - redirects to error view, when there is error thrown. Handles:
  - NoResourceFoundException - for invalid urls
  - UnknownElementException - custom exception - when a row in the database is not found
  - FeignException - errors thrown from FeignClient
  - Exception - used for any other exceptions thrown

- **NotificationFallback** - when the `NotificationClient` microservice is not running, it stops the application from returning
an error, and instead returns empty list.


##  Structure

```
Game-Pool/
├── logs  # keeps recent errors that were caught by the logger
├── src/
│   ├── main/
│   │   ├── java/project/
│   │   │   ├── config/         # Configurations to the Application
│   │   │   ├── exception/      # Exception Handler and custom exceptions
│   │   │   ├── model/          # Entities
│   │   │   ├── repository/     # JPA Repositories
│   │   │   ├── security/       # contains UserDetails class used as a session
│   │   │   ├── service/        # Entity Services
│   │   │   ├── utils/          # Global functions and FeignClient
│   │   │       └── client_dtos/       # Data Transfer Objects for the Microservice
│   │   │   └── web/           # Controllers of the Application and DTO`s
│   │   │       ├── admin/     # Admin side Controllers
│   │   │       └── dto/       # Data Transfer Objects
│   │   └── resources/
│   │       ├── templates/      # Thymeleaf templates
│   │       │    ├── admin/     # Admin side views
│   │       │    ├── error/     # Error views
│   │       │    ├── exception-views/     # ExceptionHandler views
│   │       │    └── fragments/     # Fragments templates called for the views (for repetitive code)
│   │       └── static/
│   │           ├── css/    # CSS files
│   │           └── js/     # JS files
│   └── test/ # All the tests
├── uploads/  # Images saved for the website
│   ├── avatars/ #Images for Users
│   └── games/ #Images for Games
└── pom.xml
```

## Integrations

- **Spring Security**
- **Spring Data JPA**
- **Spring Cache**
- **Spring Scheduling**
- **Spring Cloud OpenFeign**
- **Thymeleaf**
- **MySQL**