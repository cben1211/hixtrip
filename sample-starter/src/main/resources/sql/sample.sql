#todo 你的建表语句,包含索引
#设计思路:
#数据量2000w,数据量很大.需要使用分库分表设计,考虑使用shardingSphere中间件;考虑到数据后续的增加以及访问效率,为了提高性能,做主从读写分离配置和多级缓存;
#避免单个数据库中的数据过多和高并发访问而造成的服务器压力.使用水平分库,分为两个库order_db1和order_db2,使用单双数规则,单数存入db1,双数存入db2;
#买家频繁查询我的订单,实时性要求高,因此需要让同一个用户的数据在同一个库中.所以分库键使用订单的创建人id(create_by),
#访问数据库的表达式为order_db[创建人id%2+1]
#每张表的访问性能教佳在1000w以内,后续还有增加的可能,因此单表的数据量较大,使用分表,分为2个表order_1和_order2
#卖家频繁查询我的订单,允许秒级延迟。需要让同一个商户的订单尽可能在同一个表中,因此分表键使用订单的商户id(shop_id)
#访问表的表达式为order_X[商户id%2+1]
#订单相关的表包括了订单主表,订单明细表(与订单主表时N:1的对应关系),下单过程中需要先将商品加入购物车(用到购物车表)再生成订单,下订单时需要选择收货地址,
#用到收货地址表;生成订单后需要发货,跟踪物流,需要用到物流表;订单完成过程中,可能出现退款等售后服务,需要用到订单售后表.
#订单表中的索引,
#id为主键,使用uuid确保唯一性
#订单表中常用数据的查询条件有商户id(shop_id),创建人(create_by),订单单号(order_no),创建时间(create_time)和订单状态(order_status)
#因此将他们设计为索引,以便提高访问效率
#
#订单表
CREATE TABLE `order` (
                         `id` VARCHAR(50) NOT NULL COMMENT '订单id' COLLATE 'utf8_bin',
                         `order_no` VARCHAR(50) NULL DEFAULT NULL COMMENT '订单单号' COLLATE 'utf8_bin',
                         `shop_id` VARCHAR(50) NULL DEFAULT NULL COMMENT '商店编号' COLLATE 'utf8_bin',
                         `total_num` INT(10) NULL DEFAULT NULL COMMENT '数量合计',
                         `product_amount_total` INT(10) NULL DEFAULT NULL COMMENT '商品总价',
                         `logistics_fee` INT(10) NULL DEFAULT NULL COMMENT '运费',
                         `order_amount_total` INT(10) NULL DEFAULT NULL COMMENT '金额合计(实际付款金额)',
                         `pay_type` VARCHAR(1) NULL DEFAULT NULL COMMENT '支付类型，1、在线支付、0 货到付款' COLLATE 'utf8_bin',
                         `pay_time` DATETIME NULL DEFAULT NULL COMMENT '付款时间',
                         `consign_time` DATETIME NULL DEFAULT NULL COMMENT '发货时间',
                         `end_time` DATETIME NULL DEFAULT NULL COMMENT '交易完成时间',
                         `username` VARCHAR(50) NULL DEFAULT NULL COMMENT '用户名称' COLLATE 'utf8_bin',
                         `recipients` VARCHAR(50) NULL DEFAULT NULL COMMENT '收货人' COLLATE 'utf8_bin',
                         `recipients_mobile` VARCHAR(12) NULL DEFAULT NULL COMMENT '收货人手机' COLLATE 'utf8_bin',
                         `recipients_address` VARCHAR(200) NULL DEFAULT NULL COMMENT '收货人地址' COLLATE 'utf8_bin',
                         `weixin_transaction_id` VARCHAR(30) NULL DEFAULT NULL COMMENT '交易流水号' COLLATE 'utf8_bin',
                         `order_status` INT(10) NULL DEFAULT NULL COMMENT '订单状态,0:未完成,1:已完成，2：已退货',
                         `pay_status` INT(10) NULL DEFAULT NULL COMMENT '支付状态,0:未支付，1：已支付，2：支付失败',
                         `is_delete` INT(10) NULL DEFAULT NULL COMMENT '是否删除',
                         `update_time` DATETIME NULL DEFAULT NULL COMMENT '订单更新时间',
                         `create_time` DATETIME NULL DEFAULT NULL COMMENT '订单创建时间',
                         `create_by` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人' COLLATE 'utf8_bin',
                         PRIMARY KEY (`id`) USING BTREE,
                         INDEX `create_time` (`create_time`) USING BTREE,
                         INDEX `create_by` (`create_by`) USING BTREE,
                         INDEX `status` (`order_status`) USING BTREE,
                         INDEX `shop_id` (`shop_id`) USING BTREE,
                         INDEX `order_no` (`order_no`) USING BTREE
)
    COLLATE='utf8_bin'
ENGINE=InnoDB
;



#订单明细表
CREATE TABLE `order_sku` (
                             `id` VARCHAR(50) NOT NULL COMMENT 'ID' COLLATE 'utf8_bin',
                             `spu_id` VARCHAR(60) NULL DEFAULT NULL COMMENT 'SPU_ID' COLLATE 'utf8_bin',
                             `sku_id` VARCHAR(60) NULL DEFAULT NULL COMMENT 'SKU_ID' COLLATE 'utf8_bin',
                             `order_id` VARCHAR(50) NOT NULL COMMENT '订单ID' COLLATE 'utf8_bin',
                             `name` VARCHAR(200) NULL DEFAULT NULL COMMENT '商品名称' COLLATE 'utf8_bin',
                             `price` INT(10) NULL DEFAULT NULL COMMENT '单价',
                             `num` INT(10) NULL DEFAULT NULL COMMENT '数量',
                             `discount_amount` DECIMAL(20,6) NULL DEFAULT NULL COMMENT '折扣金额',
                             `money` DECIMAL(20,6) NULL DEFAULT NULL COMMENT '总金额',
                             `image` VARCHAR(200) NULL DEFAULT NULL COMMENT '图片地址' COLLATE 'utf8_bin',
                             `remark` VARCHAR(200) NULL DEFAULT NULL COMMENT '备注' COLLATE 'utf8_bin',
                             `create_time` DATE NULL DEFAULT NULL COMMENT '创建时间',
                             `create_by` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人' COLLATE 'utf8_bin',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `item_id` (`sku_id`) USING BTREE,
                             INDEX `order_id` (`order_id`) USING BTREE
)
    COLLATE='utf8_bin'
ENGINE=InnoDB
;
#购物车表
CREATE TABLE `order_shoppingcart` (
                                      `id` INT(10) NOT NULL AUTO_INCREMENT,
                                      `user_id` VARCHAR(50) NULL DEFAULT NULL COMMENT '用户编号' COLLATE 'utf8mb4_general_ci',
                                      `shop_id` VARCHAR(50) NULL DEFAULT NULL COMMENT '商店编号' COLLATE 'utf8mb4_general_ci',
                                      `product_id` VARCHAR(50) NULL DEFAULT NULL COMMENT '商品编号' COLLATE 'utf8mb4_general_ci',
                                      `is_product_exists` INT(10) NULL DEFAULT NULL COMMENT '是否有效(0:有效;1无效)',
                                      `number` INT(10) NULL DEFAULT NULL COMMENT '购买数量',
                                      `created_time` DATE NULL DEFAULT NULL COMMENT '创建时间',
                                      `create_by` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `shop_id` (`shop_id`) USING BTREE,
                                      INDEX `user_id` (`user_id`) USING BTREE
)
    COMMENT='购物车表'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

#收货地址表
CREATE TABLE `order_address` (
                                 `id` INT(10) NOT NULL AUTO_INCREMENT,
                                 `user_id` VARCHAR(50) NOT NULL COMMENT '用户编号' COLLATE 'utf8mb4_general_ci',
                                 `realname` VARCHAR(50) NOT NULL COMMENT ' 收件人姓名' COLLATE 'utf8mb4_general_ci',
                                 `telphone` VARCHAR(20) NULL DEFAULT NULL COMMENT '联系电话' COLLATE 'utf8mb4_general_ci',
                                 `country` VARCHAR(20) NULL DEFAULT NULL COMMENT '国家 ' COLLATE 'utf8mb4_general_ci',
                                 `province` VARCHAR(20) NULL DEFAULT NULL COMMENT '省份 ' COLLATE 'utf8mb4_general_ci',
                                 `city` VARCHAR(20) NULL DEFAULT NULL COMMENT '城市 ' COLLATE 'utf8mb4_general_ci',
                                 `area` VARCHAR(50) NULL DEFAULT NULL COMMENT '地区 ' COLLATE 'utf8mb4_general_ci',
                                 `street` VARCHAR(200) NULL DEFAULT NULL COMMENT '街道/详细收货地址' COLLATE 'utf8mb4_general_ci',
                                 `is_default_address` CHAR(2) NULL DEFAULT NULL COMMENT '是否默认收货地址' COLLATE 'utf8mb4_general_ci',
                                 `create_time` DATE NULL DEFAULT NULL COMMENT '创建时间',
                                 `create_by` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人' COLLATE 'utf8mb4_general_ci',
                                 PRIMARY KEY (`id`) USING BTREE
)
    COMMENT='收货地址'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;
#订单物流表
CREATE TABLE `order_logistics` (
                                   `id` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
                                   `order_id` VARCHAR(50) NULL DEFAULT NULL COMMENT '订单id' COLLATE 'utf8mb4_general_ci',
                                   `express_no` VARCHAR(20) NULL DEFAULT NULL COMMENT '发货快递单号' COLLATE 'utf8mb4_general_ci',
                                   `order_no` VARCHAR(20) NULL DEFAULT NULL COMMENT '订单号' COLLATE 'utf8mb4_general_ci',
                                   `consignee_realname` VARCHAR(50) NULL DEFAULT NULL COMMENT '收货人姓名' COLLATE 'utf8mb4_general_ci',
                                   `consignee_telphone` VARCHAR(50) NULL DEFAULT NULL COMMENT ' 联系电话' COLLATE 'utf8mb4_general_ci',
                                   `consignee_address` VARCHAR(50) NULL DEFAULT NULL COMMENT '收货地址' COLLATE 'utf8mb4_general_ci',
                                   `logistics_type` VARCHAR(50) NULL DEFAULT NULL COMMENT '物流方式' COLLATE 'utf8mb4_general_ci',
                                   `logistics_id` VARCHAR(20) NULL DEFAULT NULL COMMENT '物流商家编号' COLLATE 'utf8mb4_general_ci',
                                   `logistics_fee` DECIMAL(20,6) NULL DEFAULT NULL COMMENT '物流发货运费',
                                   `agency_fee` DECIMAL(20,6) NULL DEFAULT NULL COMMENT '快递代收货款费率',
                                   `delivery_amount` DECIMAL(20,6) NULL DEFAULT NULL COMMENT '物流成本金额',
                                   `orderlogistics_status` INT(10) NULL DEFAULT NULL COMMENT '物流状态',
                                   `logistics_settlement_status` INT(10) NULL DEFAULT NULL COMMENT '物流结算状态',
                                   `logistics_describe` VARCHAR(200) NULL DEFAULT NULL COMMENT '物流描述' COLLATE 'utf8mb4_general_ci',
                                   `logistics_create_time` DATE NULL DEFAULT NULL COMMENT '发货时间',
                                   `logistics_update_time` DATE NULL DEFAULT NULL COMMENT '物流更新时间',
                                   `logistics_settlement_time` DATE NULL DEFAULT NULL COMMENT '物流结算时间',
                                   `create_time` DATE NULL DEFAULT NULL COMMENT '创建时间',
                                   `create_by` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人' COLLATE 'utf8mb4_general_ci'
)
    COMMENT='订单物流表'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;
#订单售后表
CREATE TABLE `order_service` (
                                 `id` INT(10) NOT NULL AUTO_INCREMENT,
                                 `service_no` VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '退货编号' COLLATE 'utf8mb4_general_ci',
                                 `order_id` VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '订单编号' COLLATE 'utf8mb4_general_ci',
                                 `express_no` VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '物流单号' COLLATE 'utf8mb4_general_ci',
                                 `consignee_realname` VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '收货人姓名' COLLATE 'utf8mb4_general_ci',
                                 `consignee_telphone` VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '联系电话' COLLATE 'utf8mb4_general_ci',
                                 `consignee_address` VARCHAR(100) NOT NULL DEFAULT '0' COMMENT '收货地址' COLLATE 'utf8mb4_general_ci',
                                 `logistics_type` VARCHAR(20) NOT NULL DEFAULT '0' COMMENT '物流方式' COLLATE 'utf8mb4_general_ci',
                                 `logistics_fee` INT(10) NOT NULL DEFAULT '0' COMMENT '物流发货运费',
                                 `orderlogistics_status` INT(10) NULL DEFAULT '0' COMMENT '物流状态',
                                 `returns_type` INT(10) NULL DEFAULT '0' COMMENT '退货类型',
                                 `handling_way` VARCHAR(50) NULL DEFAULT '0' COMMENT ' 退货处理方式' COLLATE 'utf8mb4_general_ci',
                                 `returns_amount` DECIMAL(20,6) NULL DEFAULT '0' COMMENT '退款金额',
                                 `return_submit_time` DATE NULL DEFAULT NULL COMMENT '退货申请时间',
                                 `handling_time` DATE NULL DEFAULT NULL COMMENT '退货处理时间 ',
                                 `reason` VARCHAR(200) NULL DEFAULT '0' COMMENT '退货原因' COLLATE 'utf8mb4_general_ci',
                                 `create_time` DATE NULL DEFAULT NULL COMMENT '创建时间',
                                 `create_by` VARCHAR(50) NULL DEFAULT '0' COMMENT '创建人' COLLATE 'utf8mb4_general_ci',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `order_id` (`order_id`) USING BTREE,
                                 INDEX `express_no` (`express_no`) USING BTREE,
                                 INDEX `create_time` (`create_time`) USING BTREE
)
    COMMENT='订单售后表'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

