1. Project Title: Dormie, housing application for student
1. Application Description:
 A. Motivation and goal
 One of the most important topics that all students worry about when they enter university is finding 
 dorms and other housing options. They often seek accommodations via social networks and other third-party websites and groups.They can look for places to
 stay via real estate apps, but those are not tailored specifically for the needs of a student. Thus, our team came up with a mobile app called **Dormie**
 to tackle this problem.
 B. Proposed solution
 Dormie will act as a middleman who brings both house owners and students together. With the help of our application, it would only take students 3 steps
 to find their ideal accommodation: search for their school location, choose one from the recommendation list generated using information provided by the
 students when they first created their account, then submit their request to the lessor. Once negatiation between two parties reaches a satifactory
 conclusion, the request is marked as completed, and further transactions will happen outside the application.
 There are two types of users, `tenant` and `lessor`.
 - Users who are `tenant` can create, update, and delete their filter settings. They can also have multiple filter configurations for each use case, and
 view leasing posts with required information. They can submit requests to let leaseholders know they are interested.
 - Users who are `lessor` can post, update, and delete posts related to their renting properties. Each post can have address, pictures, price range, etc.
 Map API can be used to collect the location and services to store data on the database. They can also see interested `tenant` and chat with them.

3. Functionalities:
- Authentication (Login/Sign up/Logout/Delete Account) (Email & Google)
- Lessor allow to Create Read Update Remove Properties list
- Tenant can see the list of properties (base on filters)
- Tenant can create a filter
- User will be notify if baterry is low & wifi is not connected
- User can chat to each other (real-time)
- User can see the location, can search for auto-complete location, and can see the distance and direction between 2 locations
- Lessor can paid to be promoted (which means posted will be display prior if it is in the tenant search result)
- Tenant can edit filter form
- User can see the profile
- Lessor can add and remove images in their post

4. Technology used:
- Java (Android development)
- Material 3 (UI)
- View binding, Navigation component (help build app faster)
- Firebase:
  - Authentication API: authenticate
  - Firestore database: store app info
  - Firebase storage: store images
  - Firebase Function: to host micro-service for payment for promotion feature)
  - Geo-Fire & Volley Firebase: distance filter for Map
  - Place API: extract place location & information
  - Google Map API: display map
  - Place Autocompletion: to do autocompletion of location when user search for something
  - Geohasing: To filter location base on maximumm distance
- Stripe API: handle payment for promotion
- Service (Download avatar image and perform async task Firebase)
- Boardcast receiver (notify low batery and no wifi connection)
- Chat (realtime database) (event listener) using Firebase

5. Limitation, Open issues and known bugs: 
- Might have issue related to navigation between pages.
- Chat room cannot update last message when still on the same page (but will be fine when refresh)
- Could not handle images greater than 2642980 bytes.

6. Team Members:
Ho Le Minh Thach - s3877980
Nguyen Vu Thuy Duong - s3865443
Le Minh Quan - s3877969
Pham Thanh Nam - s3878413

7. Work Distribution
- Authentication: (Thach & Quan)
- Lessor can search for properties (Quan & Duong)
- Lessor allow to Create Read Update Remove Properties list (Thach)
- Tenant can see the list of properties (base on filters) (Thach)
- Tenant can create a filter (Thach & Quan)
- User will be notify if baterry is low & wifi is not connected (Quan)
- User can chat to each other (real-time) (Thach & Quan & Duong & Nam)
- User can see the location, and search for auto-complete location (Thach)
- User can see the distance and direction between 2 locations  (Duong)
- Lessor can paid to be promoted (which means posted will be display prior if it is in the tenant search result) (Thach)
- Tenant can edit filter form (Quan & Thach)
- User can see the profile (Duong & Thach)
- Lessor can add and remove images in their post (Thach)
- Detail post view (Thach & Duong)

Number of task person name appear on a task / Total number of person involve in a task.
Thach 11/23 = 47.8%
Quan 6/23 = 26.1%
Duong 5/23 = 21.8%
Nam 1/23 = 4.3%
