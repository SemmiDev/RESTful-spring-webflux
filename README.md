~~~
    © 2021 Sammidev
~~~

# API Spec  

## Create Student
~~~
    port : 8080
~~~

Request :
- Method : POST
- Endpoint : `api/v1/students`
- Body :

```json 
{
    "name": "sammidev",
    "email": "sammidev@gmail.com",
    "phone": "089239732597",
    "path": "SBMPTN"
}
```

```json
{
    "name": "sammidev update",
    "email": "sammidev@gmail.com",
    "phone": "089239732597",
    "path": "MANDIRI"
}
```

Response :

```json 
{
    "id": "5ff506d72b9cc5239f36147f",
    "name": "sammidev",
    "nim": "31130921430",
    "email": "sammidev@gmail.com",
    "phone": "089239732597",
    "path": "SBMPTN",
    "createdAt": "2021-01-06T00:39:51.113+00:00",
    "updatedAt": null
}
```


## Get Student By ID

Request :
- Method : GET
- Endpoint : `api/v1/students/5ff509212b9cc5239f361480`

Response :

```json
{
    "id": "5ff509212b9cc5239f361480",
    "name": "sammidev update",
    "nim": "31252188273",
    "email": "sammidev@gmail.com",
    "phone": "089239732597",
    "path": "MANDIRI",
    "createdAt": "2021-01-06T00:49:37.572+00:00",
    "updatedAt": null
}
```


## Get All Students

Request :
- Method : GET
- Endpoint : `api/v1/students`

Response :

```json 
[
   {
        "id": "5ff506d72b9cc5239f36147f",
        "name": "sammidev",
        "nim": "31130921430",
        "email": "sammidev@gmail.com",
        "phone": "089239732597",
        "path": "SBMPTN",
        "createdAt": "2021-01-06T00:39:51.113+00:00",
        "updatedAt": null
    }
]
```




## Update Student

Request :
- Method : PUT
- Endpoint : `api/v1/students/5ff506d72b9cc5239f36147f`
- Body :

```json 
{
    "name": "sammidev update",
    "email": "sammidev@gmail.com",
    "phone": "089239732597",
    "path": "SNMPTN"
}
```

Response : 
```json
{
    "id": "5ff506d72b9cc5239f36147f",
    "name": "sammidev update",
    "nim": "31130921430",
    "email": "sammidev@gmail.com",
    "phone": "089239732597",
    "path": "SBMPTN",
    "createdAt": "2021-01-06T00:39:51.113+00:00",
    "updatedAt": "2021-01-06T00:42:17.181+00:00"
}
```
## Delete Student

Request :
- Method : DELETE
- Endpoint : `api/v1/students/5ff506d72b9cc5239f36147f`
 
Response : 
~~~
    200 OK
~~~