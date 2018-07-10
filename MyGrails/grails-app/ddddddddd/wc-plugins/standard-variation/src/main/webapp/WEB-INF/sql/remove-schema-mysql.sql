DROP TABLE IF EXISTS `svariation_details`;
DELETE FROM product_variation_variation_option WHERE product_variation_options_id IN(SELECT id FROM product_variation WHERE details_id IN(SELECT id FROM variation_details WHERE model='standard'));
DELETE FROM product_variation WHERE details_id IN(SELECT id FROM variation_details WHERE model='standard');
DELETE FROM variation_details where model='standard';