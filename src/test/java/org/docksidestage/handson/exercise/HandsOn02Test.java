package org.docksidestage.handson.exercise;

import javax.annotation.Resource;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn02Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

    public void test_existsTestData() throws Exception {
        // ## Arrange ##

        // ## Act ##
        int count = memberBhv.selectCount(cb ->{
        });
        // ## Assert ##
        assertTrue(count > 0);
    }
}
// 自分でディレクトリをコマンドで作成したらパッケージの設定がうまくいかない。パッケージを作成してから、その中にファイルを作成するとうまく作成できた。