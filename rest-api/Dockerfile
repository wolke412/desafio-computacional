# require golang version
FROM golang:1.23

# sets the folder name where the app is contained
WORKDIR /rest-api

# first to cache dependencies
COPY go.mod go.sum ./

# download dependencies
RUN go mod download

# copy source code
COPY . .

# this is equivalent to "npm install" Just making sure every dependency has subdependencies match
RUN go mod tidy

# in production this should change to runnin a build
CMD ["go", "run", "main.go"]

