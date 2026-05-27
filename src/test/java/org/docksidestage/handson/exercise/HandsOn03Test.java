package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * ハンズオンセクション3
 * @author taba-atsu
 */
// TODO done tabata javadocの場所がズレてる by jflute (2026/04/30)
public class HandsOn03Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

    public void test_member_start_with_s_and_before_birth_19680101() throws Exception {
        // ## Arrange ##
    	LocalDate targetDate = LocalDate.of(1968,1,1);
        
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op ->
                    op.likePrefix());
			cb.query().setBirthdate_LessEqual(targetDate);
        });
    
        // ## Assert ##
        memberList.forEach(member ->{
            LocalDate birthdate = member.getBirthdate();
            log("memberBirthdate: {}", birthdate);
            assertTrue(member.getMemberName().startsWith("S"));
            assertTrue(birthdate.isBefore(LocalDate.of(1968, 1, 2)));

            // #1on1: 1/1のままで判定するとしたらどうする？ (2026/04/30)
            //assertFalse(birthdate.isAfter(targetDate));
            //assertTrue(birthdate.isBefore(targetDate) || birthdate.isEqual(targetDate));
            //assertTrue(birthdate.isBefore(targetDate.plusDays(1)));
            
            // #1on1: UnitTestで期待値をどう表現するか？現場に寄りけり話 (2026/04/30)
            // UnitTestでは、あまりロジカルなことを書かない慣習がある。(ほぼみな同意)
            // ただ、その加減がみんな違う。
            // A. 期待値をロジックで導出せずぜんぶベタ書き、つまり人間が期待値を出して書くやり方。
            // B. ある程度ロジックで導出してそれなりにコードの汎用性を持たせるやり方。
            //
            // ハンズオンはわりと、javatry的トレーニングもあるので、わりと "B" 寄りではある。
            // (ちなみに、jfluteはわりと "B" 寄り)
        });
    }

    public void test_member_status_and_member_security_info() throws Exception {
        // ## Arrange ##

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            cb.query().addOrderBy_Birthdate_Asc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        memberList.forEach(member -> {
            log("birthdate={}, memberId={}", member.getBirthdate(), member.getMemberId());
            // TODO done tabata assertNotNull()が実質的に働いていない。でもチェックはできてる。 by jflute (2026/04/30)
            // 実際、データがなかった場合、get() で NonSetupSelectRelationAccessException
            // なので、assertNotNull()まで到達していない。
            // (ちなみに、Optionalのget()は絶対にnullを戻さないメソッド)
            // だったら、極端な話、member.getMemberStatus().get() だけでも良いとも言える。
            // たまたま、get()のthrowする例外でUnitTestのチェックが成り立っていただけ。
            //
            // ただ、しっかりassertするやり方は？
            // e.g.
            //  OptionalEntity<MemberStatus> optStatus = member.getMemberStatus();
			//  boolean present = optStatus.isPresent();
			//  assertTrue(present);
            //   ↓ (変数をinline化すると)
            //  assertTrue(member.getMemberStatus().isPresent());
            //
            // 要は、Optionalで戻ってくるということは、nullをチェックしても意味がない。
            // Optionalのpresent/emptyをチェックしないと。
            //
            assertTrue(member.getMemberStatus().isPresent());
            assertTrue(member.getMemberSecurityAsOne().isPresent());

            // done jflute 1on1にて、カージナリティの話 (2026/04/30)
            // #1on1: カージナリティってなんですか？ (2026/04/30)
            // DBにおいてカージナリティと言う言葉が使われる箇所が2つある。
            // A. テーブル間のカージナリティ (リレーションシップ)
            // B. カラムのカージナリティ (値の種類数)
            // 今回は "A" のカージナリティ。

            // done tabata すべての会員が会員ステータスが存在すること前提の実装ですが... by jflute (2026/04/30)
            // それって物理的に何か保証されているのでしょうか？ (存在すること前提は合っている)
            // (会員ステータスが見つからない会員はいない理由は？)
            // 会員ステータスコードのカラムはスキーマでNOT NULLになっており、なにかしらの値が入ることが保証されている。
            // さらにそのカラムに対して外部キー制約があるので、全ての会員に対して実在するstatusが存在することが保証される。
            // NOT NULLで値があることを保証し、FKで参照先を保証している。
            // #1on1: Good, なので「すべての会員が会員ステータスが存在すること」前提でOK (2026/05/13)
            
            // done tabata すべての会員が会員セキュリティが存在すること前提の実装ですが... by jflute (2026/04/30)
            // それって物理的に何か保証されているのでしょうか？ (存在すること前提は合っている)
            // (会員セキュリティが見つからない会員はいない理由は？)
            // security レコード から見て、対応する member が必ず存在するは保証されているが、
            // member → security の方向ではDBのレイヤーで物理的に保証されていない。
            // 今回はテーブルのコメントと、テストデータ投入の際に担保している？
            // #1on1: コメント見つけたの素晴らしい (2026/05/13)
            // "会員一人につき必ず一つのセキュリティ情報がある"
            // 人間の決め事としてそのようにしている。(論理制約、業務制約)
            // テストデータ投入の際に担保...担保はないかもだけど、人間が気をつけてる。
            // ReplaceSchemaのTakeFinallyの機能を使って担保することはできる。
            // 最終チェック (TakeFinally) - データの整合性チェック | DBFlute
            // https://dbflute.seasar.org/ja/manual/function/generator/task/replaceschema/takefinally.html#assert
            
            // #1on1: ERDのリレーションシップ線 (2026/05/13)
            // 黒丸の運用。

            // #1on1: テーブル間カージナリティのレベル (2026/05/13)
            // first level: 1:1, 1:n   // 会話ではこれだけで終わることが多い
            // second level: 1:必ず1, 1:いないかもしれない1, 1:0..n, 1:1..n // 実装ではここまで必要
            
            // #1on1: 企画段階でこう考えたのに、既存DBの形が合わない話 (2026/05/13)
            // 事業会社だとあるあるで、当初のビジネスと今のビジネスでだいぶ違ってきてるときも。
            // でも、DBは当初のビジネスを想定した形になってて、今のビジネスに無理やり合わせてるときも。
            // 対策:
            // A. あらかじめ変化を想定したDB設計をしておく
            //  → でも、限界がある。想定すればするほどそのときの開発コストが掛かる。
            //  → だいたい、開発当初というのはベンチャーでめちゃ急いでる
            //
            // B. DBを変更できる仕組み作り体制作りをしておく
            //  → その1ツールとして、DBFlute
            //  → 今のビジネスにちょっとずつ合わせていく習慣
            //
            // A/B両方やっておくのがオススメ。
        });
    }
    
    public void test_member_security_reminder_question() throws Exception {
        // ## Arrange ##

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().queryMemberSecurityAsOne()
                    .setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });

        // ## Assert ##
        // #1on1: こっちはこっちで思い出。n+1のままの実装。 (2026/04/30)
        memberList.forEach(member -> {
        	// アサートするために別途検索処理を入れても誰も文句は言わない
        	// ※修行++: 実装できたら、(もし複数回検索していたら) Assert内の検索が一回になるようにしてみましょう。
        	// その際、Act内で検索しているデータを、Assert内でもう一度検索することなく実現してみましょう。
            MemberSecurity security = memberSecurityBhv.selectEntityWithDeletedCheck(cb -> {
                cb.query().setMemberId_Equal(member.getMemberId());
            });
            log("memberId={}, answer={}", member.getMemberId(), security.getReminderQuestion());
            assertTrue(security.getReminderQuestion().contains("2"));
        });
        // N+1が発生してしまうのが気になるが、どうすればいいのか思いつかなかった、、
        // DDLをみたら会員一人につき必ず一つのセキュリティ情報があるというコメントがあったので、AssertではselectEntityWithDeletedCheckを使用した
        
        // #1on1: 普段の開発でn+1は注意しろと言われるらしい (2026/04/30)
        // UnitTestなので割り切れる面もあるけど、ちょっとやめたいところ。どうやめるのか？
        // いまの実装で何が起きているかと言うと？全体で何回SQL発行してる？
        // memberListが4だとしたら？ → 4 + 1 = 5 (n+1)
        // 4の分を1にしたい。4って何を取ってる？ → どんなセキュリティ？
        // → memberIdに対応してる、どこのmemberId → Actで取得したmemberId(たち)
        // → つまり、「memberListに対応するセキュリティたちを取っている」
        // これって、一回のSQLで取れないだろうか？

        // #1on1: n+1はわかっているわけだから、n+1のnってどういうデータなのか？を日本語で表現してみる (2026/04/30)
        // その n の本質がわかれば自然とじゃあこうすればいいのかな？ってのが思いつく。
        // 「memberListに対応するセキュリティたちを取っている」がわかれば、そこから先に進める。
        // この思考のプロセスが大事。
        
        // done tabata 対応するセキュリティたちを使って、もういっこn+1を回避したアサートを書いてみましょう by jflute (2026/04/30)
        // memberSecurityBhv.selectList(cb -> {
			// memberListに対応する、って条件
        	// memberListからIDをリストで抽出するメソッドを使いたい by たばたさん
        	// まずは、かっこいいやり方は飛ばして、ベタなやり方を考える。
        	// e.g.
			//  List<Integer> memberIdList = new ArrayList<>();
			//  for (Member member : memberList) {
			//      Integer memberId = member.getMemberId();
			//      memberIdList.add(memberId);
			//  }
        	// これをベースにもうちょいどうにか。hint: step8を思い出して。
        	// 今回やりたいのは、「Listはそのままで値を変換(map)したい」ってこと。

        	// #1on1: まずベタなやり方を考える。ベタなやり方でその本質を把握する。 (2026/04/30)
        	// (ベタなやり方で提出はしないけど、本質把握のために考える)
        	
        	// done tabata Stream API でやる実装は自分でやってみましょう by jflute (2026/04/30)
        	//cb.query().setMemberId_InScope(memberIdList);

		// });

        // #1on1: n+1問題回避Good。(2026/05/27)
        List<Integer> memberIdList = memberList.stream()
                .map(member -> member.getMemberId())
                .collect(Collectors.toList());
        // #1on1: DBFluteとしては、こういうメソッドもある: extract...() (2026/05/27)
        // List<Integer> ... = memberBhv.extractMemberIdList(memberList);
        // List<String> ... = memberBhv.extractMemberAccountList(memberList);
        List<MemberSecurity> securityList = memberSecurityBhv.selectList(cb -> {
            cb.query().setMemberId_InScope(memberIdList);
        });

        // #1on1: 件数が一致していることをアサートするとさらに堅くなる (2026/05/27)
        //  e.g. assertEquals(memberIdList.size(), securityList.size());
        assertHasAnyElement(securityList);
        securityList.forEach(security -> {
            log("memberId={}, answer={}", security.getMemberId(), security.getReminderQuestion());
            assertContains(security.getReminderQuestion(), "2");
        });

        // #1on1: 模範の色々な実装も見てみましょう話 (2026/05/27)
    }
    
    public void test_member_status_order() throws Exception {
        // ## Arrange ##

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc(); // e.g. 業務要件
            cb.query().addOrderBy_MemberId_Desc(); // e.g. 固定化のため
            // #1on1: 最後PKで並べるパターン話 (2026/05/27)
            // order byがユニークにならないケース。
            // 同じ正式会員同士でどう並べるか？が指定されていないと...
            // 検索するたびに入れ替わっちゃう可能性がある。
            // データが変わってないのに、画面リロードのたびにデータの表示が変わるのはノイズ。
            // なので、固定化するために最後(業務的にはどうでもいいんだけど)PKで並べる。
            // 要は、画面検索においてユニークにならないorder byをすることはほとんどない。
        });
    
        // ## Assert ##
        assertHasAnyElement(memberList);
        // TODO tabata 複数のものを入れるオブジェクトなので、変数名もなんか複数を示したい by jflute (2026/05/27)
        //  e.g. existingStatusCodes, existingStatusCodeSet
        Set<String> existingStatusCode = new HashSet<>();
        String previousStatusCode = null;
        for (Member member : memberList) {
        	// TODO tabata 必ずってわけじゃないけど、currentを付けたいかも by jflute (2026/05/27)
        	// currentとpreviousでおもっきり比較しているので、その区別を明確にした方が直感的かなと。
            String statusCode = member.getMemberStatusCode();
            log("memberId={}, statusCode={}", member.getMemberId(), statusCode);

            assertFalse(member.getMemberStatus().isPresent());

            if (!statusCode.equals(previousStatusCode)) {
                assertFalse(existingStatusCode.contains(statusCode));
                // TODO tabata ifの中じゃなくてもいいものは、ifの外に出した方が読み手の負担が少ない by jflute (2026/05/27)
                // せっかくSetなので、とにかく既出ということで突っ込んで、重複弾きはSetにお任せでも良い。
                // 現状、重複弾きを自力でやってるので、厳密にはListでも重複が発生しない。Setの機能を使ってないとも言える。
                // previousの解釈、塊の1個前なのか？ループの1個前なのか？後者にしても良い。AABBCC
                existingStatusCode.add(statusCode);
                previousStatusCode = statusCode;
            }
        }
        // #1on1: わかりやすい日本語↓素晴らしい (2026/05/27)
        // 固まって並んでいるところに関して、同じステータスコードの会員が連続した1ブロックに並んでること。
        // なので、ループを回してコードが前と変わった瞬間に出てきたコードが、既出のものではないかを検知する方針にした
        
        // #1on1: 他人のコードを読む時は思いついたりするのに、自分でコード書くときにおもいつかなかったり... by たばたさん (2026/05/27)
        // 自分のコードを、いかに客観的に見るかに掛かってる。
        // 時間があれば、ちょっとコードを寝かせて後で見ると、短期記憶に頼らないreadingができる。
        // 三日間はちょっと無理でも、10分30分後に見るだけでもだいぶ違うかも。それが自己レビュー。
        //
        // 別に、プルリクレビューの前にレビューしてもらっていいんだからね
        // https://jflute.hatenadiary.jp/entry/20170630/reviewbefore
        //
        // 「3. 実装し終わった直後自己レビュー」がちょうどそれ。
        // もの作りスキル。
    }

    public void test_purchase_of_member_having_birthdate() throws Exception {
        // ## Arrange ##
    	// #1on1: テストみたいに難易度を読む勘繰りが効きにくいように突然シンプルな問題もある (2026/05/27)

        // ## Act ##
    	// #1on1: 基点テーブルを間違えてないの素晴らしい (2026/05/27)
    	// 要件の日本語の解釈大事。助詞が大事。
    	// DBFluteは基点テーブルを重視している。そこの第一歩を間違えなこと。
        List<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Product();
            cb.query().queryMember().setBirthdate_IsNotNull();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
            cb.query().addOrderBy_PurchasePrice_Desc();
            cb.query().addOrderBy_ProductId_Asc();
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(purchase -> {
            Member member = purchase.getMember().get();
            String memberName = member.getMemberName();
            String memberStatusName = member.getMemberStatus().get().getMemberStatusName();
            String productName = purchase.getProduct().get().getProductName();
            log("memberName={}, memberStatusName={}, productName={}", memberName, memberStatusName, productName);

            assertNotNull(member.getBirthdate());
        });
    }


}
