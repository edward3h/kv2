# User entities

## Identity
Represents a user from an external provider, e.g. Google or Discord.
Used for authentication.

## User
Internal user model for this application.
In theory multiple identities could map to a single user, but there is no implementation for that yet.
A user will always have at least one identity, since that is how they are created.

### Roles
Users have roles, which are used to determine what they can do.
In implementation, roles map to Micronaut Security roles.

#### _Anonymous_
Not a role, but the absence of any roles. i.e. not authenticated.
Can view public parts of the site, and any documents that the owners have made public.

#### ROLE_USER
Every authenticated user has this role from creation.
Can do everything _Anonymous_ can. 
Can view documents the owners have made readable to users (?)
Can edit documents that they own.
Can edit their own display name and profile picture.

#### ROLE_SUPERUSER
Currently, only available by directly running a database update.
Can view and edit all documents and users.

# User Use Cases

## Sign In/Sign Up
State: no authenticated user.

* Click 'sign in/sign up'
* Displays list of login provider links. e.g. Google, Discord.
* Click provider. [or cancel?]
* Redirected through Oauth procedure...
* Handle oauth success
  * look up identity
  * Identity exists 
    * login as user
    * redirect to home page or page they were on
  * Identity does not exist
    * create new identity and user
    * login as user
    * redirect to profile page and/or terms & conditions (?)
    * user's initial profile name and picture are extracted from the identity, but they are able to edit them
* Handle oauth failure
  * Too bad (?)

## Sign Out
State: authenticated user.

* Click 'sign out'
* User is signed out.
* Stay on same page if it is viewable by _Anonymous_
* Otherwise, redirect to home page.

## Edit Profile
State: authenticated user.

* Click link (somewhere?) to go to profile page.
* Text field to edit display name.
* Image selector and upload form for profile picture.

## _TODO: Moderation?_
_May want the ability to check or block text and pictures with inappropriate content._