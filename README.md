# Sample API using Scalar DB and Spring Boot
Scalar DBとSpring Bootを使ったサンプルAPIです。

## API エンドポイント

| URI | HTTPメソッド | Description |
| --- | ---------- | ------------ |
| /users | POST | ユーザーを登録する |
| /users | GET | ユーザーを一覧取得する |
| /users/{user_id} | GET | ユーザー情報を取得する |
| /users/{user_id} | PUT | ユーザー情報を更新する |
| /users/{user_id} | DELETE | ユーザーを削除する |
| /groups | POST | グループを登録する |
| /groups | GET | グループ一覧を取得する |
| /groups/{group_id}/group-users | PUT | グループにユーザーを追加する|
| /groups/{group_id}/group-users | GET | グループに所属するユーザーを一覧取得する|
| /groups/{group_id}/group-users/{user_id} | PUT | グループからユーザーを脱退させる |
| /groups/{group_id} | DELETE | グループを削除する |

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
