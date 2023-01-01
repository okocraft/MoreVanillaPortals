# MoreVanillaPortals

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/okocraft/MoreVanillaPortals)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/okocraft/MoreVanillaPortals/build.yml?branch=master)
![GitHub](https://img.shields.io/github/license/okocraft/MoreVanillaPortals)

ネザーポータルとエンドポータルのバニラ機能を、カスタムワールドでも使えるようにするプラグインです。

通常のサーバーではデフォルトのワールドでのみポータル移動ができますが、 このプラグインは `<worldname>` `<worldname>_nether` `<worldname>_the_end`
のそれぞれのワールド間でも移動できるようにします。

## 使い方

1. [このページ](https://github.com/okocraft/MoreVanillaPortals/releases) から Jar ファイルをダウンロードする
2. `plugins` ディレクトリに Jar ファイルを移動して起動する
3. オーバーワールドのワールド名 `name` に対して `name_nether`, `name_the_end` のワールドを用意する

## 制約

- プレイヤーを除くエンティティのポータル移動
- 任意のワールド名同士でのポータル移動設定
