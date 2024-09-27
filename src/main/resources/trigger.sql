
UPDATE trippydb.post p
    LEFT JOIN (
    SELECT l.post_id, COUNT(l.post_id) AS like_count
    FROM post_like l
    GROUP BY l.post_id
    ) lc ON p.post_id = lc.post_id
    LEFT JOIN (
    SELECT c.post_id, COUNT(c.post_id) AS comment_count
    FROM post_comment c
    GROUP BY c.post_id
    ) cc ON p.post_id = cc.post_id
    SET p.score = (COALESCE(lc.like_count, 0) * 0.7 + COALESCE(cc.comment_count, 0) * 0.3)
WHERE 1=1;


DELIMITER $$

-- 트리거: post_like 테이블에 행이 삽입될 때
CREATE TRIGGER after_post_like_insert
    AFTER INSERT ON post_like
    FOR EACH ROW
BEGIN
    UPDATE post p
        LEFT JOIN (
        SELECT l.post_id, COUNT(l.post_id) AS like_count
        FROM post_like l
        GROUP BY l.post_id
        ) lc ON p.post_id = lc.post_id
        LEFT JOIN (
        SELECT c.post_id, COUNT(c.post_id) AS comment_count
        FROM post_comment c
        GROUP BY c.post_id
        ) cc ON p.post_id = cc.post_id
        SET p.score = (COALESCE(lc.like_count, 0) * 0.7 + COALESCE(cc.comment_count, 0) * 0.3)
    WHERE p.post_id = NEW.post_id;
    END$$

    -- 트리거: post_like 테이블에서 행이 삭제될 때
    CREATE TRIGGER after_post_like_delete
        AFTER DELETE ON post_like
        FOR EACH ROW
    BEGIN
        UPDATE post p
            LEFT JOIN (
            SELECT l.post_id, COUNT(l.post_id) AS like_count
            FROM post_like l
            GROUP BY l.post_id
            ) lc ON p.post_id = lc.post_id
            LEFT JOIN (
            SELECT c.post_id, COUNT(c.post_id) AS comment_count
            FROM post_comment c
            GROUP BY c.post_id
            ) cc ON p.post_id = cc.post_id
            SET p.score = (COALESCE(lc.like_count, 0) * 0.7 + COALESCE(cc.comment_count, 0) * 0.3)
        WHERE p.post_id = OLD.post_id;
        END$$

        DELIMITER ;


DELIMITER $$

        CREATE TRIGGER after_post_comment_insert
            AFTER INSERT ON post_comment
            FOR EACH ROW
        BEGIN
            UPDATE post p
                LEFT JOIN (
                SELECT l.post_id, COUNT(l.post_id) AS like_count
                FROM post_like l
                GROUP BY l.post_id
                ) lc ON p.post_id = lc.post_id
                LEFT JOIN (
                SELECT c.post_id, COUNT(c.post_id) AS comment_count
                FROM post_comment c
                GROUP BY c.post_id
                ) cc ON p.post_id = cc.post_id
                SET p.score = (COALESCE(lc.like_count, 0) * 0.7 + COALESCE(cc.comment_count, 0) * 0.3)
            WHERE p.post_id = NEW.post_id;
            END$$

            DELIMITER ;

DELIMITER $$

            CREATE TRIGGER after_post_comment_delete
                AFTER DELETE ON post_comment
                FOR EACH ROW
            BEGIN
                UPDATE post p
                    LEFT JOIN (
                    SELECT l.post_id, COUNT(l.post_id) AS like_count
                    FROM post_like l
                    GROUP BY l.post_id
                    ) lc ON p.post_id = lc.post_id
                    LEFT JOIN (
                    SELECT c.post_id, COUNT(c.post_id) AS comment_count
                    FROM post_comment c
                    GROUP BY c.post_id
                    ) cc ON p.post_id = cc.post_id
                    SET p.score = (COALESCE(lc.like_count, 0) * 0.7 + COALESCE(cc.comment_count, 0) * 0.3)
                WHERE p.post_id = OLD.post_id;
                END$$

                DELIMITER ;


