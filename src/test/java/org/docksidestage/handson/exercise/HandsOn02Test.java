package org.docksidestage.handson.exercise;

import java.util.List;
import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
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

    public void test_memberName() throws Exception {
        // ## Arrange ##

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb ->{
            cb.query().setMemberName_LikeSearch("S", op ->
                    op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });
        // ## Assert ##
        // TODO done tabata もし検索が0件のとき、ループが素通りしてアサートが動かずgreenになってしまうので... by jflute (2026/01/07)
        // テストデータが0件の時の話とか、検索条件がバグって0件とか、そういうも検知したい
        // なので、memberListが最低限1件はあるよね、ってのをアサートしたい。
        //  e.g. assertFalse(memberList.isEmpty());
        // もうハンズオンでは専用メソッドを用意しているので、assH 補完でOK。これ使ってくださいませ。
        //  e.g. assertHasAnyElement(memberList);
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
                	// TODO done tabata getMemberName()を2回呼び出しているので...些細なことですが、変数抽出してみましょう by jflute (2026/01/07)
                    // IntelliJでサクッとできるはずなので、やってみてください。(control+T でリファクタリングメニュー)
            String memberName = member.getMemberName();
            log("memberName: {}", memberName);
                    assertTrue(memberName.startsWith("S"));
        });
    }

    public void test_memberId() throws Exception {
        // ## Arrange ##
    	// TODO jflute 次回1on1にて、Optionalの話、javatryのstep8と連動 (2026/01/07)

        // ## Act ##
    	// TODO done tabata OptionalEntityの変数、DBFluteのスタイルに合わせてもらえたらと by jflute (2026/01/07)
    	// https://dbflute.seasar.org/ja/manual/function/ormapper/behavior/select/selectentity.html#optionalname
        OptionalEntity<Member> optMember = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(1);
        });
        // ## Assert ##
        // #1on1: 問答無用get()は意図しているか？(たばたさんsaid:たぶんifの書き忘れ?) (2026/01/07)
        // でも、ここはget()でOK。テストなので1番が存在しなかったら論外なので落ちていい場面だから。
        // ifを書くってことは「なかった場合の処理」を書く必要があるということになるけど、ここではその必要ない。
        // というように、常にifを意識しないといけないわけではなく、落ちてもいいやってときは問答無用でもOK。
        // (厳密には、デバッグのしやすさとかもあるのでもうちょい複雑にはなるが、それはOptionalの話のときに一緒に)
        Member member = optMember.get();
        // TODO tabata "expected:<2> but was:<1>", 期待値が逆 by jflute (2026/01/07)
        assertEquals(Integer.valueOf(1), member.getMemberId());
    }

    public void test_memberBirthdate() throws Exception {
        // ## Arrange ##

        // ## Act ##
    	// TODO done tabata cbの左に空白空いてる "( cb" by jflute (2026/01/07)
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Desc();
        });
        // ## Assert ##
        memberList.forEach( member -> {
                    log("memberBirthdate: {}", member.getBirthdate());
                    assertNull(member.getBirthdate());
        });
    }
}
// 自分でディレクトリをコマンドで作成したらパッケージの設定がうまくいかない。パッケージを作成してから、その中にファイルを作成するとうまく作成できた。