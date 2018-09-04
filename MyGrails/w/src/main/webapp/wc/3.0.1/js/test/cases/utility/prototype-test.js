$(function() {
    jasmine.Sandbox.addSpec("Extended Split Test", function() {
        jasmine.it("split plain", function() {
            var splitted = "1,23,we,you".exSplit();
            jasmine.expect(splitted.length).toBe(4)
            jasmine.expect(splitted[2]).toBe("we");
        })
        jasmine.it("split values with comma", function() {
            var splitted = "1,(2,3),we,you".exSplit();
            jasmine.expect(splitted.length).toBe(4)
            jasmine.expect(splitted[1]).toBe("2,3");
        })
        jasmine.it("first value start with comma", function() {
            var splitted = "(,1),23,we,you".exSplit();
            jasmine.expect(splitted.length).toBe(4)
            jasmine.expect(splitted[0]).toBe(",1");
        })
        jasmine.it("last value end with comma", function() {
            var splitted = "1,23,we,(you,)".exSplit();
            jasmine.expect(splitted.length).toBe(4)
            jasmine.expect(splitted[3]).toBe("you,");
        })
        jasmine.it("middle value start with comma", function() {
            var splitted = "1,(,23),we,you".exSplit();
            jasmine.expect(splitted.length).toBe(4)
            jasmine.expect(splitted[1]).toBe(",23");
        })
        jasmine.it("middle value end with comma", function() {
            var splitted = "1,(23,),we,you".exSplit();
            jasmine.expect(splitted.length).toBe(4)
            jasmine.expect(splitted[1]).toBe("23,");
        })
        jasmine.it("first empty value", function() {
            var splitted = ",1,23,we,you".exSplit();
            jasmine.expect(splitted.length).toBe(5)
            jasmine.expect(splitted[0]).toBe("")
        })
        jasmine.it("last empty value", function() {
            var splitted = "1,23,we,you,".exSplit();
            jasmine.expect(splitted.length).toBe(5)
        })
    });
    jasmine.Sandbox.addSpec("Array Test", function() {
        jasmine.it("contains true", function() {
            var result = [45, "ghji", 9, 10].contains("ghji");
            jasmine.expect(result).toBeTruthy()
        })
        jasmine.it("contains false", function() {
            var result = [45, "ghji", 9, 10].contains(12);
            jasmine.expect(result).toBeFalsy()
        })
    });
    jasmine.Sandbox.addSpec("Html Encode And Text", function() {
        jasmine.it("check encode 1", function() {
            var result2 = "hello \n  brim bar".htmlEncode();
            jasmine.expect(result2).toBe("hello <br> &nbsp;brim bar");
        })
        jasmine.it("check encode 2", function() {
            var result1 = $("<div></div>").text("hello \n      brim bar").html();
            var result2 = "hello \n      brim bar".htmlEncode();
            //lowercase is required as ie8 returns all tag name capital
            jasmine.expect(result1.toLowerCase()).toBe(result2);
        })
    });
})