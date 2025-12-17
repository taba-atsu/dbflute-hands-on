package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// #1on1: DBFluteの互換モードの話、フレームワークの互換運用 (2025/12/17)

public class HandsOn02Test extends UnitContainerTestCase {

	@Resource
	private MemberBhv memberBhv;

	public void test_1on1_20251105() throws Exception {
		// _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
		// 生年月日が存在しない会員がいたら何かやる
		// _/_/_/_/
		{ // A
			int count = memberBhv.selectCount(cb -> {
				cb.query().setBirthdate_IsNull();
			});
			if (count > 0) { // 生年月日が存在しない会員が1人以上いたら
				// なにか
			}
		}
		{ // B
			ListResultBean<Member> memberList = memberBhv.selectList(cb -> { // select MEMBER_ID, ..._NAME, BIRTHDATE, ...
				cb.query().setBirthdate_IsNull();
			});
			if (!memberList.isEmpty()) { // 生年月日が存在しない会員が1人以上いたら (つまり同じ)
				// なにか
			}
			// ↑これはちょっと直感的ではない by たばたさん
		}
		{ // C
			int count = memberBhv.selectList(cb -> { // select MEMBER_ID, ..._NAME, BIRTHDATE, ...
				cb.query().setBirthdate_IsNull();
			}).size();
			if (count > 0) { // 生年月日が存在しない会員が1人以上いたら (つまり同じ)
				// なにか
			}
			// ↑これなら少しselectCount()に近づいた？ by jflute
		}
		/*
  o
 /｜\ -+                  +------------------- Server World -----------------------------+
  /\   |                  |                                                              |
       |                  |          +-----------------+                      /----\     |
 +-----------+            |          |      Java       |        (SQL)        /      \    |
 | Browser   |            |          |                 | ------------------> |  DB   |   |
 |           |            |          |                 |                     |       |   |
 |  HTML     |            |          |                 |     JDBC            |       |   |
 |   Control |            |          |                 |                     |       |   |
 |    |      |            |          |                 |                     |       |   |
 |    v      |    (HTTP request)     |                 |                     |       |   |
 |  Data     | --------------------> |     MEMORY      |                     | HDD   |   |
 |   Control |                       |                 |                     |       |   |
 |           | <-------------------- | (HTML Template) | <------------------ |       |   |
 +-----------+    (HTTP response)    +-----------------+        (data)       +-------+   |
 JavaScript               |                   ^    ^                                     |
 (HTML Template)          |                   |    |                                     |
      ^                   +------------------/T\--/T\------------------------------------+ 
      |                                       |    |
      |  (request)                            |    |
      |-------Ajax----------------------------+    |
        (JSON response)                            |
                                                   |
 +--------+    (request)                           |
 | iPhone | <--------------------------------------+
 +--------+  (JSON response) e.g. {"name": "jflute", "favorite": "sea"}
		 */
		// #1on1: DB検索は別のマシンのハードディスクを探っている意識 (2025/11/05)
		// 「［改訂新版］プロになるためのWeb技術入門」
	}

	public void test_existsTestData() throws Exception {
		// ## Arrange ##

		// ## Act ##
		int count = memberBhv.selectCount(cb -> {
		});
		// ## Assert ##
		assertTrue(count > 0);
	}
	
	// #1on1: マスターテーブルとは？ (2025/11/05)
	// 広義のマスターと狭義のマスター。
	// e.g. プロジェクト、データベース
}
// 自分でディレクトリをコマンドで作成したらパッケージの設定がうまくいかない。パッケージを作成してから、その中にファイルを作成するとうまく作成できた。