### Requirements

- Docker Desktop
- Java 17+
- Maven 3.8+

### Create file .env.dev in main directory
```bash
POSTGRES_DB=warehouse_db
POSTGRES_USER=warehouse_user
POSTGRES_PASSWORD=warehouse_pass
```

### Running backend in terminal
```bash
docker stop $(docker ps -aq); docker rm $(docker ps -aq)
docker system prune -a
docker compose up --build --force-recreate
```

### Backend swagger documentation
```bash
http://localhost:8080/swagger-ui/index.html
```


### Running frontend in terminal
```bash
cd warehouse-management-system
cd frontend
npm install
npm run dev
```


### Screenshots of running system
<img width="1919" height="1074" alt="image" src="https://github.com/user-attachments/assets/42f4e7c9-28c5-43ae-9d52-b463d2f16146" />

#### 1. Warehouse Worker (Magazynier)

* **Product Management** – Adding, editing, and organizing items in the warehouse.
<img width="1919" height="1071" alt="image" src="https://github.com/user-attachments/assets/d94396d9-6d45-4579-99e4-7cedb3f84deb" />

* **Inventory Replenishment** – Restocking goods and maintaining optimal inventory levels.
<img width="1919" height="1076" alt="image" src="https://github.com/user-attachments/assets/27cb44f1-4112-48c0-a465-94c45f0e4213" />

* **Delivery Management** – Processing incoming and outgoing shipments.
<img width="1919" height="1072" alt="image" src="https://github.com/user-attachments/assets/bf33a404-bd4a-4b3b-9a19-fb5fcc6e9ce8" />

#### 2. Administrator

* **Employee Management** – Creating and managing user accounts and staff permissions.
<img width="1919" height="1074" alt="image" src="https://github.com/user-attachments/assets/75e97545-8f7f-4a75-80da-35510528a890" />

* **Payroll Management** – Overseeing salaries and payment processing.
<img width="1919" height="1075" alt="image" src="https://github.com/user-attachments/assets/710fe3f9-e98f-432e-9209-8b02f8fbb1a9" />

* **Operation History** – Full access to audit logs and tracking all system activities.
<img width="1919" height="1079" alt="image" src="https://github.com/user-attachments/assets/0d22db3b-2f13-4e94-8d55-8346f9a4afdc" />

