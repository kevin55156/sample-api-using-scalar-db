# Sample API using Scalar DB and Spring Boot
https://github.com/yoshi-kino/sample-api-using-scalar-db
を元に、マルチストレージでScalarDBを利用できるAPIです。
- コード中とファイル名のuserとmovieを全置換(小文字と大文字に注意)することでモデル名の変更が可能なことを確認しました。
- 認証なしでAPIを叩けるように改造しました。)

## API エンドポイント

| URI | HTTPメソッド | Description |
| --- | ---------- | ------------ |
| /users | POST | ユーザーを登録する |
| /users | GET | ユーザーを一覧取得する |
| /users/{user_id} | GET | ユーザー情報を取得する |
| /users/{user_id} | PUT | ユーザー情報を更新する |
| /users/{user_id} | DELETE | ユーザーを削除する |
| /movies/{user_id} | POST | グループを登録する |
| /movies | GET | グループ一覧を取得する |
| /movies/{movie_id}/movie-users | PUT | 動画にユーザーを関連づける|
| /movies/{movie_id}/movie-users | GET | 動画に関連するユーザーを一覧取得する|
| /movies/{movie_id}/movie-users/{user_id} | PUT | 動画とユーザーの関連を外す |
| /movies/{movie_id} | DELETE | 動画を削除する |

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
