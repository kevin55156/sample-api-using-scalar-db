# Sample API using Scalar DB and Spring Boot
https://github.com/yoshi-kino/sample-api-using-scalar-db
を元に、マルチストレージでScalarDBを利用できるAPIです。
- コード中とファイル名のuserとmovieを全置換(小文字と大文字に注意)することでモデル名の変更が可能なことを確認しました。
- 認証なしでAPIを叩けるように改造しました。)

## API エンドポイント

| URI | HTTPメソッド | Description |
| --- | ---------- | ------------ |
| /users | POST | register a user |
| /users | GET | get users'imformation|
| /users/{user_id} | GET | get user's imformation  |
| /users/{user_id} | PUT | update user's imformation |
| /users/{user_id} | DELETE | delete a user|
| /movies/{user_id} | POST | register a movie |
| /movies | GET | get movies' imformation |
| /movies/{movie_id}/movie-users | PUT | associate a movie with a user|
| /movies/{movie_id}/movie-users | GET | get movie's imformation　associated with a movie |
| /movies/{movie_id}/movie-users/{user_id} | PUT | update association between a movie and a user |
| /movies/{movie_id} | DELETE | delete a movie |

bodyのjsonの詳細については/api/src/main/java/config/example/api/dtoを参照してください。

# Creating a Schema
以下のコマンドで、Cassandraを起動させ、スキーマを作成してください。

``` bash
$ ./db up
```

# Launch the API
以下のコマンドで、APIを起動させてください。

``` bash
$ cd api
$ ./gradlew bootRun
```
