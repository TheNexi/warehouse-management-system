### Requirements

- Docker Desktop
- Java 17+
- Maven 3.8+


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
npm install react-router-dom
npm run dev
```