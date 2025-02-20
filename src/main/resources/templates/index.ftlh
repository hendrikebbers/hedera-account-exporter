<!DOCTYPE html>
<html lang="en">
<#include "common/head.ftlh">
<body>
<#include "common/bodyStart.ftlh">
<main class="container">
    <#include "common/menu.ftlh">
  <section>
    <h2>Welcome</h2>
    <form action="${'/'}" method="POST">
      <div class="form-group">
        <label for="accountId">AccountId</label>
        <input type="text" class="form-control" id="accountId" name="accountId" value="${accountId!""}"/>
      </div>
      <input type="submit" value="Set Account"/>
    </form>
  </section>
</main>
</body>
</html>