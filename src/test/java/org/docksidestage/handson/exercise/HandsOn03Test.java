package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.unit.UnitContainerTestCase;

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
        
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op ->
                    op.likePrefix());
            cb.query().setBirthdate_LessEqual(LocalDate.of(1968,1,1));
        });
    
        // ## Assert ##
        memberList.forEach(member ->{
            LocalDate birthdate = member.getBirthdate();
            log("memberBirthdate: {}", birthdate);
            assertTrue(member.getMemberName().startsWith("S"));
            assertTrue(birthdate.isBefore(LocalDate.of(1968, 1, 2)));
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
            assertNotNull(member.getMemberStatus().get());
            assertNotNull(member.getMemberSecurityAsOne().get());
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
        memberList.forEach(member -> {
            MemberSecurity security = memberSecurityBhv.selectEntityWithDeletedCheck(cb -> {
                cb.query().setMemberId_Equal(member.getMemberId());
            });
            log("memberId={}, answer={}", member.getMemberId(), security.getReminderQuestion());
            assertTrue(security.getReminderQuestion().contains("2"));
        });
        // N+1が発生してしまうのが気になるが、どうすればいいのか思いつかなかった、、
        // DDLをみたら会員一人につき必ず一つのセキュリティ情報があるというコメントがあったので、AssertではselectEntityWithDeletedCheckを使用した
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
