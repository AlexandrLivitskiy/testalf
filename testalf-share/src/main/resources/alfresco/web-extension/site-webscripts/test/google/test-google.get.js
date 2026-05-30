function main() {
   var result = {};
   try {
      var connector = remote.connect("google");
      var resp = connector.get("/robots.txt");
      result.status = resp.status;
      result.body = resp.response;
   } catch (e) {
      result.status = 500;
      result.body = e.toString();
   }
   model.result = result;
}
main();
