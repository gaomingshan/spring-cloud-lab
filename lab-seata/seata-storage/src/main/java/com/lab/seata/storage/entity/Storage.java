package com.lab.seata.storage.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("t_storage")
public class Storage {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long productId;
    private Integer total;
    private Integer used;
    private Integer residue;
}
