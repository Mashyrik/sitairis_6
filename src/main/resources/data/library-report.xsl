<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lib="http://sitairis.com/library" exclude-result-prefixes="lib">
  <xsl:output method="html" encoding="UTF-8" indent="yes"/>
  <xsl:template match="/">
    <html lang="ru"><head><meta charset="UTF-8"/><title>Отчёт библиотеки</title>
      <style>body{font-family:Arial,sans-serif;margin:32px;color:#1f2937}h1,h2{color:#17365d}table{border-collapse:collapse;width:100%;margin-bottom:28px}th,td{border:1px solid #d1d5db;padding:8px;text-align:left}th{background:#17365d;color:white}tr:nth-child(even){background:#f3f6fa}.overdue{color:#b91c1c;font-weight:bold}.metric{display:inline-block;margin-right:24px}</style>
    </head><body><h1>Отчёт информационной системы библиотеки</h1>
      <p><span class="metric">Читателей: <b><xsl:value-of select="count(lib:library/lib:readers/lib:reader)"/></b></span>
      <span class="metric">Книг: <b><xsl:value-of select="count(lib:library/lib:books/lib:book)"/></b></span>
      <span class="metric">Выдач: <b><xsl:value-of select="count(lib:library/lib:loans/lib:loan)"/></b></span>
      <span class="metric">Просрочено: <b><xsl:value-of select="count(lib:library/lib:loans/lib:loan[lib:status='OVERDUE'])"/></b></span></p>
      <h2>Каталог книг</h2><table><tr><th>ID</th><th>Название</th><th>Автор</th><th>Год</th><th>Статус</th></tr>
        <xsl:for-each select="lib:library/lib:books/lib:book"><xsl:sort select="lib:title"/>
          <tr><td><xsl:value-of select="@id"/></td><td><xsl:value-of select="lib:title"/></td><td><xsl:value-of select="lib:author"/></td><td><xsl:value-of select="lib:publicationYear"/></td><td><xsl:value-of select="lib:status"/></td></tr>
        </xsl:for-each></table>
      <h2>Выдачи</h2><table><tr><th>ID</th><th>Читатель</th><th>Книга</th><th>Выдана</th><th>Срок</th><th>Статус</th><th>Штраф</th></tr>
        <xsl:for-each select="lib:library/lib:loans/lib:loan"><xsl:sort select="lib:dueDate"/>
          <tr><td><xsl:value-of select="@id"/></td><td><xsl:value-of select="lib:readerId"/></td><td><xsl:value-of select="lib:bookId"/></td><td><xsl:value-of select="lib:issueDate"/></td><td><xsl:value-of select="lib:dueDate"/></td>
          <td><xsl:if test="lib:status='OVERDUE'"><xsl:attribute name="class">overdue</xsl:attribute></xsl:if><xsl:value-of select="lib:status"/></td><td><xsl:value-of select="format-number(lib:fineAmount,'0.00')"/> BYN</td></tr>
        </xsl:for-each></table>
    </body></html>
  </xsl:template>
</xsl:stylesheet>
