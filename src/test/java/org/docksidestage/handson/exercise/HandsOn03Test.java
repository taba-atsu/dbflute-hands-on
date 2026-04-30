package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// TODO tabata javadocの場所がズレてる by jflute (2026/04/30)
public class HandsOn03Test extends UnitContainerTestCase {
	/**
	 * ハンズオンセクション3
	 * @author taba-atsu
	 */

    @Resource
    private MemberBhv memberBhv;
    private MemberSecurityBhv memberSecurityBhv;
    
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
            // TODO tabata assertNotNull()が実質的に働いていない。でもチェックはできてる。 by jflute (2026/04/30)
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
			assertNotNull(member.getMemberStatus().get());
            assertNotNull(member.getMemberSecurityAsOne().get());

            // TODO jflute 1on1にて、カージナリティの話 (2026/04/30)
            // #1on1: カージナリティってなんですか？ (2026/04/30)
            // DBにおいてカージナリティと言う言葉が使われる箇所が2つある。
            // A. テーブル間のカージナリティ (リレーションシップ)
            // B. カラムのカージナリティ (値の種類数)
            // 今回は "A" のカージナリティ。

            // TODO tabata すべての会員が会員ステータスが存在すること前提の実装ですが... by jflute (2026/04/30)
            // それって物理的に何か保証されているのでしょうか？ (存在すること前提は合っている)
            // (会員ステータスが見つからない会員はいない理由は？)
            
            // TODO tabata すべての会員が会員セキュリティが存在すること前提の実装ですが... by jflute (2026/04/30)
            // それって物理的に何か保証されているのでしょうか？ (存在すること前提は合っている)
            // (会員セキュリティが見つからない会員はいない理由は？)
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
        
        // TODO tabata 対応するセキュリティたちを使って、もういっこn+1を回避したアサートを書いてみましょう by jflute (2026/04/30)
        memberSecurityBhv.selectList(cb -> {
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
        	
        	// TODO tabata Stream API でやる実装は自分でやってみましょう by jflute (2026/04/30)
        	//cb.query().setMemberId_InScope(memberIdList);
		});
    }
    
    public void test_member_status_order() throws Exception {
        // ## Arrange ##

        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });
    
        // ## Assert ##
//        memberList.forEach(member -> {
//            log("memberId={}, displayOrder={}",member.getMemberId(),);
//            assertNull(member.getMemberStatus());
//        });
    }
}
