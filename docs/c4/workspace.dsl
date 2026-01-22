workspace "Order transaction Platform" "C4 Context Diagram" {

    model {
        user = person "Utilisateur" "Users of Barbichetz"

        // Context : rendu/context.png
        transactionPlatform = softwareSystem "Transaction order platform" "Allows users to create/verify transaction"  "Internal" {
            // Zoom in the container of the transaction plateform : rendu/container.png
            frontend = container "Frontend Web" "Frontend app that provides user to create/verify transactions" "React.js"  "front"

            apiGateway = container "API Gateway" "Single entry point - Routing." "REST API" 

            authService = container "Auth Service" "User management using Keycloack server." "REST API" "back" {

                // Zoom in the component authentification : rendu/component_authentification.png
                authController = component "Auth Controller" "Get management endpoints" "REST Controller"

                authServiceLogic = component "Auth Service" "Business logic for users and authentication/creation accoint"

                keycloakAdapter = component "Keycloak Server" "Integration with Keycloak server"

                userRepository = component "User Repository" "Persistence for users"

                authController -> authServiceLogic "Calls"
                authServiceLogic -> keycloakAdapter "Authenticate users"
                authServiceLogic -> userRepository "Data users"
            }

            userDb = container "User Database" "Storage for users and table keys." "MySQL" "Database"

            transactionService = container "Metadata Service" "Transaction handling, upload, signatures." "REST API" "back" {

                // Zoom in the component transaction : rendu/component-transaction.png
                transactionController = component "Transaction Controller" "Endpoints REST to create or verify transactions" "REST Controller"

                transactionServiceLogic = component "Transaction Service" "Creating or verifying transactions logic"

                vaultAdapter = component "Vault Adapter" "Calls Vault Transit Engine for hashing, signing and verifying"

                minioAdapter = component "MinIO Adapter" "Calls MinIO for video uploading and retrieving"

                transactionRepository = component "Transaction Repository" "Persistence of metadata of transactions"

                transactionController -> transactionServiceLogic "Calls"
                transactionServiceLogic -> vaultAdapter "Hash / Sign / Verify"
                transactionServiceLogic -> minioAdapter "Store / Retrieve video"
                transactionServiceLogic -> transactionRepository "Db transaction metadata"
            }

            transactionDb = container "Transaction Database" "Storage for transaction metadata and signatures." "MySQL" "Database"

            notificationService = container "Notification service" "Allows users to get notification on transaction statut changed" "REST API" "back"{
                
                // Zoom in the component notification : rendu/component_notification.png
                notificationController = component "Notification Controller" "Expose notification endpoints" "REST Controller"

                notificationServiceLogic = component "Notification Service" "Notification business logic"

                notificationSender = component "Notification Sender" "Send notifications"

                notificationRepository = component "Notification Repository" "Persistence for notifications"

                notificationController -> notificationServiceLogic "Calls"
                notificationServiceLogic -> notificationSender "Send notification"
                notificationServiceLogic -> notificationRepository "Store notifications"
            }

            notificationDb = container "Notification Batabase" "Storage for notifications." "MySQL" "Database"
        }

        vault = softwareSystem "Vault transit engine" "SK/PK Key generator." "External,kms"

        minio = softwareSystem "MinIO Object Storage" "Video storage" "External,storage"

        // Relations between service 
        user -> frontend "Users create/verify transactions using"
        frontend -> authService "Authenticate or create account using"
        frontend -> apiGateway "Proceeds API calls using"
        apiGateway -> authService "Verify JWT and authorisation using"
        apiGateway -> transactionService "Create/verify transaction order using"
        apiGateway -> notificationService "Get notifications using"

        // Relations between service and database
        authService -> userDb "Reads from and writes user data using"
        transactionService -> transactionDb "Reads from and writes transaction metadata using"
        notificationService -> notificationDb "Reads from and writes notification data using"

        // Relations between service and external service
        authService -> vault "Generate SK / PK using"
        transactionService -> vault "Video hash signing and verification using"

        transactionService -> minio "Store and retrieve videos using"
    }

    views {

        container transactionPlatform {
            include *
            // autolayout lr    // => In order to personalize the vieew, we can comment this line and modify the position as like as we want
        }

        component notificationService {
            include *
            // autolayout lr
        }

        component authService {
            include *
            // autolayout lr
        }
        component transactionService {
            include *
            // autolayout lr
        }

        styles {

            element "Person" {
                shape person
                background #08427b
                color #ffffff
            }

            element "Internal" {
                background #1168bd
                color #ffffff
            }

            element "External" {
                background #eeeeee
                color #000000
                border dashed
            }

            element "Database" {
                shape Cylinder
                background #999999
                color #ffffff
                icon "./icons/mysql.png"
            }

            element "front" {
                icon "./icons/react.png"
            }

            element "back" {
                icon "./icons/spring.png"
            }

            element "kms" {
                icon "./icons/vault.png"
            }

            element "storage" {
                icon "./icons/miniIO.jpg"
            }
        }

        theme default
    }
}
