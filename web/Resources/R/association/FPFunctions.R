options(sqldf.driver = "SQLite")


constraints <- c('traffic_protective_measure_name=NO APLICA'
                 ,'traffic_alcohol_level_counterpart_name=SIN DATO'
                 ,'traffic_involved_vehicle_victim_name=SIN DATO'
                 ,'traffic_service_type_victim_name=SIN DATO'
                 ,'service_type_counterpart_one_types_name=NO APLICA'
                 ,'service_type_counterpart_two_types_name=NO APLICA'
                 ,'service_type_counterpart_three_types_name=NO APLICA'
                 ,'involved_vehicle_counterpart_one_vehicles_name=NO APLICA'
                 ,'involved_vehicle_counterpart_one_vehicles_name=SIN DATO'
                 ,'involved_vehicle_counterpart_two_vehicles_name=NO APLICA'
                 ,'involved_vehicle_counterpart_three_vehicles_name=NO APLICA'
                 ,'fatal_place_name=NO SE SABE'
                 ,'victim_ethnic_group_name=NINGUNO'
                 ,'suicide_mental_antecedent_name=NO SE SABE'
                 ,'suicide_previous_attempt_name=NO SE SABE'
                 ,'suicide_related_event_name=SIN DATO'
                 ,'accident_accidents_non_fatal_victims_name=0'
                 ,'time_name=Desconocido'
                 ,'neighborhood_suburb_name=SIN DATO URBANO'
                 ,'time_group=Desconocido'
                 ,'residence_neighborhood_suburb_name=SIN DATO URBANO'
                 ,'victim_age_five_year=Desconocido'
                 ,'victim_gender=Desconocido'
                 ,'job_jobs_category_name=SIN DATO'
                 ,'self_inflicted_factor_name=NO SE SABE'
                 ,'self_inflicted_previous_attempt_name=NO SE SABE'
                 ,'self_inflicted_mental_antecedent_name=NO SE SABE'
                 ,'interpersonal_context_name=NO SE SABE'
                 ,'interpersonal_relationship_victim_name=NO SE SABE'
                 ,'interpersonal_aggressor_gender_name=NO SE SABE'
                 ,'interpersonal_previous_antecedent_name=NO SE SABE'
                 ,'interpersonal_relationship_victim_name=DESCONOCIDO'
                 )
constraints <- as.data.frame(constraints)

pattern <- c("DESCONOCIDO","SIN DATO", "NO APLICA", "NO SE SABE")

coding <- function(data){
  columns <- ncol(data)
  values <- vector()
  for (i in seq(1,columns)){
    the_levels <- setdiff(toupper(levels(data[,i])), pattern)
    values <- c(values, paste0(names(data)[i],'=', the_levels))
  }
  dict <- as.data.frame(values)
  dict <- sqldf("SELECT * FROM dict EXCEPT SELECT * FROM constraints")
  dict$keys <- seq(1, nrow(dict))
  names(dict) <- c('VALUE', 'KEY')
  
  return(dict)
}

codingByRow <- function(data){
  columns <- ncol(data)
  values <- vector()
  for (i in seq(1,columns)){
    values <- c(values, paste0(names(data)[i],'=', data[1,i]))
  }
  return(values)
}

write.translated <- function(translated, filename){
  write.table(translated, filename, row.names=F, col.names=F, quote=F)
}

transactionize <- function(data){
  n <- nrow(data)
  transactions <- data.table()
  for (i in seq(1,n)){
    row <- codingByRow(data[i,])
    id <- rep(i, length(row))
    transaction <- data.table(ID=id, ROW=row)
    #transactions <- rbind(transactions,transaction)
    l <- list(transactions,transaction)
    transactions <- rbindlist(l)
  }
  return(transactions)
}

transactionize_to_file <- function(data, filename){
  n <- nrow(data)
  transactions <- data.table()
  write.table('', filename, row.names=F, col.names=F, quote=F)
  for (i in seq(1,n)){
    row <- codingByRow(data[i,])
    id <- rep(i, length(row))
    transaction <- data.table(ID=id, ROW=row)
    write.table(transaction, filename, row.names=F, col.names=F, quote=F, append=T)
  }
  transactions <- as.data.table(str_split(readChar(filename, file.info(filename)$size), '\\n')[[1]])
  return(transactions)
}

translating <- function(data, dict){
  transactions <- transactionize(data)
  translated <- sqldf('SELECT group_concat(KEY," ") AS keys FROM (SELECT ID, KEY FROM transactions JOIN dict ON VALUE = ROW) AS F GROUP BY ID')
  return(translated)
}

translating_to_file <- function(data, dict){
  transactions <- transactionize_to_file(data, '/tmp/test.txt')
  translated <- sqldf('SELECT group_concat(KEY," ") AS keys FROM (SELECT ID, KEY FROM transactions JOIN dict ON VALUE = ROW) AS F GROUP BY ID')
  return(translated)
}

## Not used

translatingByValue <- function(value, dict){
  sql <- paste0('SELECT key FROM dict WHERE value LIKE "',value,'"')
  key <- sqldf(sql)$KEY
  return(key)
}

translatingByKey <- function(key, dict){
  return(sqldf(paste0('SELECT value FROM dict WHERE key = ',key))$VALUE)
}

translatingByRow <- function(row, dict){
  translated <- vector()
  for (value in row){
    code <- translatingByValue(value, dict)
    translated <- c(translated, code)
  }
  return(translated)
}
