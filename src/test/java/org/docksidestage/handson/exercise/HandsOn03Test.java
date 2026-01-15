package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn03Test extends UnitContainerTestCase {
    /**
     * ハンズオンセクション3
     * @author taba-atsu
     */

    @Resource
    private MemberBhv memberBhv;
    
    public void test_member_start_with_s_and_before_birth_19680101() throws Exception {
        // ## Arrange ##
        
        // ## Act ##
        List<Member> memberList = memberBhv.selectList(cb ->{
            cb.query().setMemberName_LikeSearch("S", op ->
                    op.likePrefix());
            cb.query().setBirthdate_LessEqual(LocalDate.of(1968,1,1));
        });
    
        // ## Assert ##
    }
}
