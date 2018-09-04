#### How to use macro in Order Email for Gift Wrapper?

```
%each:order_details>items:item:index%
    <tr>
        <td>
            %if:item>url%<a href="%item>url%">%if% %item>product_name% %if:item>variations%(%item>variations%)%if%%if:item>url%</a>%if%
            %item>gift_wrapper_name% <br>
            %item>gift_wrapper_price%
        </td>
        <td>%currency_symbol%%item>price%</td>
        <td>%item>quantity%</td>
        <td>%currency_symbol%%item>discount%</td>
        <td>%currency_symbol%%item>tax%</td>
        <td>%currency_symbol%%item>total_with_tax_with_discount%</td>
    </tr>
%each%
```

- <b>GIFT WRAPPER NAME</b> : To show gift wrapper name add "gift_wrapper_name" macro in email html template

- <b>GIFT WRAPPER PRICE</b> : To show gift wrapper price add "gift_wrapper_price" macro in email html template
