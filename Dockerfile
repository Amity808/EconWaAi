FROM ubuntu:latest
LABEL authors="amityclev"

ENTRYPOINT ["top", "-b"]