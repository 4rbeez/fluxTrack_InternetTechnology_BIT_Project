# fluxTrack Inventory Management System

#### Contents:
- [Analysis](#analysis)
  - [Scenario](#scenario)
  - [User Stories](#user-stories)
  - [Use Case](#use-case)
- [Design](#design)
  - [Prototype Design](#prototype-design)
  - [Domain Design](#domain-design)
  - [Business Logic](#business-logic)
- [Implementation](#implementation)
  - [Backend Technology](#backend-technology)
  - [Frontend Technology](#frontend-technology)
- [Project Management](#project-management)
  - [Roles](#roles)
  - [Milestones](#milestones)

## Analysis
The original project idea comes from the need of fluxed GmbH to replace manual stock communication between the company and its partners. In the existing process, partners still communicate inventory changes manually, for example through Excel sheets, e-mail, or chat. This leads to delays, missing transparency, and situations where products may still appear purchasable although they are no longer in stock. The purpose of fluxTrack is therefore to provide a central inventory management system with real-time or near-real-time stock visibility and easier maintenance of product data. 

The assessment also requires that the application demonstrates at least three layers on two tiers, at least four views, at least four entities, and at least one business rule in the service layer. The fluxTrack concept matches this very well, because it already defines a web client, backend logic, database interaction, several product- and account-related use cases, and Shopify-related data exchange.

### Scenario

fluxTrack is a web application for fluxed GmbH and its partners to manage product inventory centrally. The goal of the system is to reduce manual work, improve inventory transparency, and synchronize relevant stock data with the fluxed web shop. In the Internet Technology demonstrator, the focus is on partner login, product overview, product creation, stock adjustment, search/filter functions, and a simplified Shopify integration boundary. Different users of fluxed GmbH and its partners can receive different access rights based on their application role. 

### User Stories
(1) As an admin, I want to view all products across all partners, so that I can have a complete overview of the product range.
(2) As a partner, I want to view all products under my profile, so that I can manage and review my own product listings.
(3) As an admin or partner, I want to create new product entries, so that I can expand the product range available in the application.
(4) As an admin or partner, I want to view detailed information about a specific product, so that I can understand its attributes and details.
(5) As an admin or partner, I want to create, update, and delete products, so that I can keep the product range accurate and up to date.
(6) As an admin, I want to create new partner profiles, so that new partners can be onboarded into the system.
(7) As an admin or partner, I want to edit partner profiles, so that partner information remains accurate and up to date.
(8) As an admin, I want to view all partners, so that I can have an overview of all partner profiles in the system.

### Use Case fluxTrack

![](images/use-case.png)
- UC-1 [View all Products (Admin)]: Admin can retrieve all the Products on the product range from all Partners.
- UC-2 [View Own prodcuts (Partner)]: Partner can retreive all the Products listed under the Partner's profile.
- UC-3 [Create Product]: Admin and Partner can create new product entries in the application.
- UC-4 [View Product Details]: Admin and Partner can retrieve the information on a specific product.
- UC-5 [Edit a Product]: Admin and Partner can create, update, and delete products from the product range.
- UC-6 [Create Partner Profile]: Admin can create new Partner Profiles.
- UC-7 [Edit Partner Profile]: Admin and Partner can Edit Partner Profiles.
- UC-8 [View all Partners]: Admin can see an overview of all Partners.

## Design
> 🚧: Keep in mind the Corporate Identity (CI); you shall decide appropriately the color schema, graphics, typography, layout, User Experience (UX), and so on.

### Wireframe
> 🚧: It is suggested to start with a wireframe. The wireframe focuses on the website structure (Sitemap planning), sketching the pages using Wireframe components (e.g., header, menu, footer) and UX. You can create a wireframe already with draw.io or similar tools. 

We start on the login Screen, where each user has a different login, which is linked to the profile (Partner or Admin). After Login, the user is presented with the main page, which is a product overview that either shows all products (Admin) or just the Partner specific ones (Partner).
On the top right, the user is able to add a new product via a pop-up, where they enter Product Name, SKU, Price in CHF, the current stock quantity and an estimated delivery time. Once saved, the product will be displayed on the overview, in which existing products can be edited.

### Prototype

Login Screen:
<img width="973" height="689" alt="image" src="https://github.com/user-attachments/assets/7255a249-f960-416f-9f05-152fae2eb4c7" />

Product Overview / Homepage
<img width="936" height="664" alt="image" src="https://github.com/user-attachments/assets/ee1075c0-bc08-4ab0-8fdd-695cbe6d2c0d" />

Add Product Screen
<img width="972" height="689" alt="image" src="https://github.com/user-attachments/assets/62d658ac-b925-4c2a-b7d0-4840e4aa0fab" />

### Domain Design
> 🚧: Provide a picture and describe your domain model; you may use Entity-Relationship Model or UML class diagram. Both can be created in Visual Paradigm - we have an academic license for it.

The `ch.fhnw.pizza.data.domain` package contains the following domain objects / entities including getters and setters:

![](images/domain-model.png)

### Business Logic 
> 🚧: Describe the business logic for **at least one business service** in detail. If available, show the expected path and HTPP method. The remaining documentation of APIs shall be made available in the swagger endpoint. The default Swagger UI page is available at /swagger-ui.html.

Based on the Use case description, there will be two user profiles, which have different roles and privileges.

- The Admin is able to see all products of all partners
- The Partner is only able to see the products listed under his profile

**Path**: [`/api/menu/?location="Basel"`] 

**Param**: `value="location"` Admitted value: "Basel","Brugg".

**Method:** `GET`

## Implementation
> 🚧: Briefly describe your technology stack, which apps were used and for what.

### Backend Technology
> 🚧: It is suggested to clone this repository, but you are free to start from fresh with a Spring Initializr. If so, describe if there are any changes to the PizzaRP e.g., different dependencies, versions & etc... Please, also describe how your database is set up. If you want a persistent or in-memory H2 database check [link](https://github.com/FHNW-INT/Pizzeria_Reference_Project/blob/main/pizza/src/main/resources/application.properties). If you have placeholder data to initialize at the app, you may use a variation of the method **initPlaceholderData()** available at [link](https://github.com/FHNW-INT/Pizzeria_Reference_Project/blob/main/pizza/src/main/java/ch/fhnw/pizza/PizzaApplication.java).

This Web application is relying on [Spring Boot](https://projects.spring.io/spring-boot) and the following dependencies:

- [Spring Boot](https://projects.spring.io/spring-boot)
- [Spring Data](https://projects.spring.io/spring-data)
- [Java Persistence API (JPA)](http://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html)
- [H2 Database Engine](https://www.h2database.com)

To bootstrap the application, the [Spring Initializr](https://start.spring.io/) has been used.

Then, the following further dependencies have been added to the project `pom.xml`:

- DB:
```XML
<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
</dependency>
```

- SWAGGER:
```XML
   <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.3.0</version>
   </dependency>
```

### Frontend Technology
> 🚧: Describe your views and what APIs is used on which view. If you don't have access to the Internet Technology class Budibase environment(https://inttech.budibase.app/), please write to Devid on MS teams.

This Web application was developed using Budibase and it is available for preview at https://inttech.budibase.app/app/pizzeria. 

## Execution
> 🚧: Please describe how to execute your app and what configurations must be changed to run it. 

**The codespace URL of this Repo is subject to change.** Therefore, the Budibase PizzaRP webapp is not going to show any data in the view, when the URL is not updated or the codespace is offline. Follow these steps to start the webservice and reconnect the webapp to the new webservice url. 

> 🚧: This is a shortened description for example purposes. A complete tutorial will be provided in a dedicated lecture.

1. Clone PizzaRP in a new repository.
2. Start your codespace (see video guide at: [link](https://www.youtube.com/watch?v=_W9B7qc9lVc&ab_channel=GitHub))
3. Run the PizzaRP main available at PizzaApplication.java on your own codespace.
4. Set your app with a public port, see the guide at [link](https://docs.github.com/en/codespaces/developing-in-a-codespace/forwarding-ports-in-your-codespace).
5. Create an own Budibase app, you can export/import the existing Pizzeria app. Guide available at [link](https://docs.budibase.com/docs/export-and-import-apps).
6. Update the pizzeria URL in the datasource and publish your app.

### Deployment to a PaaS
> 🚧: Deployment to PaaS is optional but recommended as it will make your application (backend) accessible without server restart and through a unique, constantly available link.  

Alternatively, you can deploy your application to a free PaaS like [Render](https://dashboard.render.com/register).
1. Refer to the Dockerfile inside the application root (FHNW-INT/Pizzeria_Reference_Project/pizza).
2. Adapt line 13 to the name of your jar file. The jar name should be derived from the details in the pom.xml as follows:<br>
`{artifactId}-{version}.jar` 
2. Login to Render using your GitHub credentials.
3. Create a new Web Service and choose Build and deploy from a Git repository.
4. Enter the link to your (public) GitHub repository and click Continue.
5. Enter the Root Directory (name of the folder where pom.xml resides).
6. Choose the Instance Type as Free/Hobby. All other details are default.
7. Click on Create Web Service. Your app will undergo automatic build and deployment. Monitor the logs to view the progress or error messages. The entire process of Build+Deploy might take several minutes.
8. After successful deployment, you can access your backend using the generated unique URL (visible on top left under the name of your web service).
9. This unique URL will remain unchanged as long as your web service is deployed on Render. You can now integrate it in Budibase to make API calls to your custom endpoints.

## Project Management
> 🚧: Include all the participants and briefly describe each of their **individual** contribution and/or roles. Screenshots/descriptions of your Kanban board or similar project management tools are welcome.

### Roles


### Milestones
1. **Analysis**: Scenario ideation, use case analysis and user story writing.
2. **Prototype Design**: Creation of wireframe and prototype.
3. **Domain Design**: Definition of domain model.
4. **Business Logic and API Design**: Definition of business logic and API.
5. **Data and API Implementation**: Implementation of data access and business logic layers, and API.
6. **Security and Frontend Implementation**: Integration of security framework and frontend realisation.
7. (optional) **Deployment**: Deployment of Web application on cloud infrastructure.


#### Maintainer
- Fabian Arnold
- Remy Brunner
- Silvan Meier
- Florian Stiegeler

#### License
- [Apache License, Version 2.0](blob/master/LICENSE)
