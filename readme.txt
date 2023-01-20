1. Project Title: Dormie, housing application for student
2. Application Description:

A. Motivation and goal
One of the most important topics that all students worry about when they enter university is finding 
dorms and other housing options. They often seek accommodations via social networks and other third-party websites and groups.They can look for places to stay via real estate apps, but those are not tailored specifically for the needs of a student. Thus, our team came up with a mobile app called **Dormie** to tackle this problem.
B. Proposed solution
Dormie will act as a middleman who brings both house owners and students together. With the help of our application, it would only take students 3 steps to find their ideal accommodation: search for their school location, choose one from the recommendation list generated using information provided by the students when they first created their account, then submit their request to the lessor. Once negatiation between two parties reaches a satifactory conclusion, the request is marked as completed, and further transactions will happen outside the application.
There are two types of users, `tenant` and `lessor`.
- Users who are`tenant` can create, update, and delete their filter settings. They can also have multiple filter configurations for each use case, and view leasing posts with required information. They can submit requests to let leaseholders know they are interested.
- Users who are `lessor` can post, update, and delete posts related to their renting properties. Each post can have address, pictures, price range, etc. Map API can be used to collect the location and services to store data on the database. They can also see interested `tenant` and chat with them.

The chatting feature will have 3 statuses: `In Process`,  `Complete` if negotiation is successful, or `Waitlist` if the property is not currently available, depending on what happens in each conversation between `lessor` and `tenant`. 

3. Functionalities:


4. Technology used:
 Dormie is developed using `Java` as programming language and `Android Studio` as IDE. Furthermore:
A. `Google Maps API` is used to provide visuals of addresses and to calculate approximate distance between properties and schools.
B.  `Google Firebase App Development` platform is used for the following features: 
    - `Authentication`: used to allow for registering new users, logging in existing users, and other users' information update protocols.
    - `Realtime Database`: used to store realtime chatting conversations between users.
    - `Firestore Storage`: used to store user information, as well as other static information such as housing posts.
Open issues and known bugs: 

5. Team Members:
Ho Le Minh Thach - s3877980
Nguyen Vu Thuy Duong - s3877969
Le Minh Quan - s3865442
Pham Thanh Nam - s3878413

5. Work Distribution

6. Open issues and known bugs

7. Demo
Link to the demo video: https://rmiteduau-my.sharepoint.com/:f:/g/personal/s3878413_rmit_edu_vn/Eq6m4Pf78i5PpS82LpI50HgBt0_rcMTdGSlpVLFUZgjFpw?e=Jsvxfk


