# Sample API using Scalar DB and Spring Boot
Scalar DBとSpring Bootを使ったサンプルAPIです。

## API エンドポイント

| URI | HTTPメソッド | Description |
| --- | ---------- | ------------ |
| /users | POST | ユーザーを登録する |
| /users | GET | ユーザーを一覧取得する |
| /users| GET | ユーザー情報を取得する |
| /users/{user_id} | PUT | ユーザー情報を更新する |
| /users/{user_id} | DELETE | ユーザーを削除する |
| /movies/{user_id} | POST | グループを登録する |
| /movies | GET | グループ一覧を取得する |
| /movies/{movie_id}/movie-users | PUT | グループにユーザーを追加する|
| /movies/{movie_id}/movie-users | GET | グループに所属するユーザーを一覧取得する|
| /movies/{movie_id}/movie-users/{user_id} | PUT | グループからユーザーを脱退させる |
| /movies/{movie_id} | DELETE | グループを削除する |

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
