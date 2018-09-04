jasmine.Sandbox.addSpec("Custom UI", function() {
    jasmine.uit("Radio Initialization", function(sandbox) {
        var radio = $("<input type='radio' name='abc' value='tuntun'>");
        sandbox.append(radio);
        radio.radio();
        jasmine.expect(radio.is(":hidden")).toBeTruthy()
        jasmine.expect(radio.next().is("span.wcui-radio")).toBeTruthy()
    })

    jasmine.uit("Radio Initialization With State Checked/UnChecked", function(sandbox) {
        var radio1 = $("<input type='radio' name='abc' value='tuntun1'>");
        var radio2 = $("<input type='radio' name='abc' value='tuntun2' checked>");
        sandbox.append(radio1);
        sandbox.append(radio2);
        radio1.add(radio2).radio();
        jasmine.expect(radio1.next().is(".checked")).toBeFalsy()
        jasmine.expect(radio2.next().is(".checked")).toBeTruthy()
    })

    jasmine.uit("Radio Selection Change", function(sandbox) {
        var radio1 = $("<input type='radio' name='abc' value='tuntun1'>");
        var radio2 = $("<input type='radio' name='abc' value='tuntun2' checked>");
        sandbox.append(radio1);
        sandbox.append(radio2);
        radio1.add(radio2).radio();
        jasmine.expect(radio1.radio("val")).toBe('tuntun2')
        radio2.radio("val", "tuntun1")
        jasmine.expect(radio1.radio("val")).toBe('tuntun1')
    })

    jasmine.uit("Radio Selection Change Ui Reflection", function(sandbox) {
        var radio1 = $("<input type='radio' name='abc' value='tuntun1'>");
        var radio2 = $("<input type='radio' name='abc' value='tuntun2' checked>");
        sandbox.append(radio1);
        sandbox.append(radio2);
        radio1.add(radio2).radio();
        radio1.radio("val", "tuntun1")
        jasmine.expect(radio1.next().is(".checked")).toBeTruthy()
        jasmine.expect(radio2.next().is(".checked")).toBeFalsy()
    })
});