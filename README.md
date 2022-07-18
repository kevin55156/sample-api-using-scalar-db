https://github.com/yoshi-kino/sample-api-using-scalar-db
を元にしました。
- 不要なファイルを削除しました。
- 不要な認証を削除しました。
- movies周りのDTO,Repository,Controller,Service,Modelを追加しました。
- マルチストレージに対応させるために、docker-composeファイルとschemaを書き換えました。

## API エンドポイント

| URI | HTTP method | Description |
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
以下のコマンドで、CassandraとMySQLを起動させ、スキーマを作成してください。

``` bash
$ ./db up
```

# Launch the API
以下のコマンドで、APIを起動させてください。

``` bash
$ cd api
$ ./gradlew bootRun
```
