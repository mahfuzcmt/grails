$(function() {
    jasmine.Sandbox.addSpec("Validation Rule Parser", function() {
        jasmine.it("multiple rule", function() {
            var rules = ValidationRule.createRules("required email fail");
            jasmine.expect(rules.length).toBe(3)
            jasmine.expect(rules[0].rule).toBe(VALIDATION_RULES.required);
        })
        jasmine.it("rule with multiple space", function() {
            var rules = ValidationRule.createRules("required    match[dfgh,3,,45] url");
            jasmine.expect(rules.length).toBe(3)
            jasmine.expect(rules[0].rule).toBe(VALIDATION_RULES.required);
        })
        jasmine.it("rule with terminating spaces", function() {
            var rules = ValidationRule.createRules("required rangelength[2,100] ");
            jasmine.expect(rules.length).toBe(2)
            jasmine.expect(rules[1].rule).toBe(VALIDATION_RULES.rangelength);
        })
        jasmine.it("rule with if and not", function() {
            var rules = ValidationRule.createRules("required@if{global:justCheck}    number@not{rule:range[12,45]}  skip");
            jasmine.expect(rules.length).toBe(3)
            jasmine.expect(rules[0].condition).toBeDefined();
            jasmine.expect(rules[0].condition.selector).toBe("justCheck");
            jasmine.expect(rules[1].condition).toBeDefined();
            jasmine.expect(rules[1].condition.type).toBe("not");
        })
        jasmine.it("rule having param with [", function () {
            var rules = ValidationRule.createRules("required@if{global:justCheck}    match[[1-9]]*]  skip");
            jasmine.expect(rules.length).toBe(3)
            jasmine.expect(rules[1].param[0]).toBe("[1-9]*");
        })
    });
    jasmine.Sandbox.addSpec("Validation Rule Check", function() {
        jasmine.it("invalid email", function() {
            jasmine.expect(VALIDATION_RULES.email.check("service@gmail")).toBeFalsy();
        })
        jasmine.it("valid email with name", function() {
            jasmine.expect(VALIDATION_RULES.email.check("Perfact Dreamer<service@gmail.com>")).toBeTruthy();
        })
        jasmine.it("multiple without name", function() {
            jasmine.expect(VALIDATION_RULES.email.check("service@gmail.com;defend@jasmine.com")).toBeTruthy();
        })
        jasmine.it("with and without name mixed", function() {
            jasmine.expect(VALIDATION_RULES.email.check("service@gmail.com;Jasmine Altaf<defend@jasmine.com>;revert@github.com")).toBeTruthy();
        })
    });
})