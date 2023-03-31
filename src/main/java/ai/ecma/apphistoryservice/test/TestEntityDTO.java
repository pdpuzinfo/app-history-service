package ai.ecma.apphistoryservice.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TestEntityDTO {

    private String name;

    private Date date;

    private Boolean ok;

    private int age;

    private List<Integer> ints;

    private Integer manyToOneId;

    private Integer oneToOneId;

    private String[] arr;

    private int[] intArr;

}
