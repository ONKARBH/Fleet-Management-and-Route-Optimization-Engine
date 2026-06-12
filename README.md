# Fleet-Management-and-Route-Optimization-Engin

📋 Project Overview
A comprehensive Fleet Management REST API with an intelligent Route Optimization Engine built with Spring Boot. The system optimizes delivery routes, manages fleet operations, tracks vehicles in real-time, and handles driver assignments with a sophisticated state machine.


🚀 Features

Week 1: Core Infrastructure ✅

· Vehicle Management: Register, update, track vehicles with maintenance schedules
· Driver Management: Manage driver profiles, licenses, and shift hours
· Delivery Task Management: Create and manage delivery tasks with time windows
· Database: MySQL with JPA/Hibernate ORM


Week 2: Route Optimization ✅

· External API Integration: OSRM (Open Source Routing Machine) for distance calculations
· TSP Algorithms: Nearest Neighbor & 2-OPT optimization algorithms
· Distance Matrix: Calculate optimal routes between multiple waypoints
· Dispatch Management: Assign deliveries to vehicles/drivers with optimized sequences



Week 3: State Management & GPS Tracking ✅

· State Machine: Delivery lifecycle management (UNASSIGNED → DISPATCHED → IN_TRANSIT → DELIVERED)
· GPS Tracking: Real-time vehicle location tracking
· Delivery Order Enforcement: Ensures deliveries are completed in optimized sequence
· Cancel/Retry Logic: Handle delivery failures and cancellations
· Mobile Endpoints: Driver-specific APIs for route viewing and status updates



Week 4: Security & Deployment (Coming Soon)

· Swagger/OpenAPI documentation
· Docker containerization
· JWT Authentication
· Production configurations

