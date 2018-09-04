<g:applyLayout name="_productwidget">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container">
            <div class="bmui-tab-header" data-tabify-tab-id="description">
                <span class="title"><g:message code="product.description"/></span>
            </div>
            <div class="bmui-tab-header" data-tabify-tab-id="video">
                <span class="title"><g:message code="video"/></span>
            </div>
            %{--<plugin:hookTag hookPoint="productInfoTabHeader" attrs="${[:]}"/>--}%
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-description">
                <span class="title">Lorem Ipsum</span>
                <span class="description">
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque blandit quis erat ut euismod.
                    Nulla fermentum, arcu at mollis porttitor, ligula augue pharetra tellus,
                    ut rhoncus metus ante rutrum risus. Vivamus volutpat odio ut leo rutrum, id porttitor quam tempor.
                    Nulla metus risus, faucibus bibendum laoreet sed, hendrerit sit amet leo.
                    In id velit vitae enim semper aliquam.
                    Maecenas vitae neque fermentum, tincidunt metus vel, lacinia enim. Pellentesque dapibus nec metus a aliquam.
                    Phasellus congue condimentum elit eget euismod. Fusce sit amet augue eu quam tincidunt gravida.
                </span>
            </div>
            <div id="bmui-tab-video">
                    %{--<g:include view="site/productVideo.gsp" model="[product: product]"/>--}%
            </div>
            %{--<plugin:hookTag hookPoint="productInfoTabBody" attrs="${[:]}"/>--}%
        </div>
    </div>
</g:applyLayout>