require("RPostgreSQL")

update_utf8 <-function(table){

driver <- dbDriver("PostgreSQL", max.con = 250)
connection <- dbConnect(driver, dbname="icfes", user= "omar", password="123", host="localhost")
sql <- paste("SELECT * FROM ",table, " LIMIT 1", sep="")
data <- dbGetQuery(connection, sql)
for (var in names(data)){
  sql1 <- paste("UPDATE ", table, "SET  ", var, " = upper ( translate(",var, ", 'áéíóúÁÉÍÓÚäëïöüÄËÏÖÜñ', 'aeiouAEIOUaeiouAEIOUÑ'));")
  dbGetQuery(connection, sql1)              
}
dbDisconnect(connection)
dbUnloadDriver(driver)
}