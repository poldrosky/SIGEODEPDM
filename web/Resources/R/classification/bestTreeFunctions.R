require("RWeka")
require("rJava")
require("stringr")
require("igraph")

getPath <- function(array){
  rule <- vector()
  for (i in seq(1, length(array) - 1)){
    node <- array[i]
    edge <- paste0(array[i], '->', array[i + 1])
    rule <- c(rule, node, edge)
  }
  rule <- c(rule, array[length(array)])
  return(rule)
}

getRule <- function(path){
  path <- getPath(path)
  n <- length(path)
  consequent <- dict[path[n]][[1]]
  consequent <- str_sub(consequent, str_locate_all(consequent, '\\"')[[1]][1,1] + 1, str_locate_all(consequent, '\\"')[[1]][2,2] - 1)
  stats <- str_sub(consequent, str_locate(consequent, '[(]')[1] + 1, str_locate(consequent, '[)]')[1] - 1)
  if (grepl('/', stats)){
    total <- as.numeric(str_split(stats, '/')[[1]][1])
    incorrects <- as.numeric(str_split(stats, '/')[[1]][2])
  } else {
    total <- as.numeric(stats)
    incorrects <- 0
  }
  corrects <- total - incorrects
  if (total == 0){
    return('')
  }
  confidence <- corrects/total
  support <- total / NRECORDS
  if (confidence < CONFIDENCE || support < SUPPORT){
    return('')
  }
  aux <- vector()
  for (dot_node in dict[path[1:n-1]]){
    aux <- c(aux, str_sub(dot_node, str_locate_all(dot_node, '\\"')[[1]][1,1] + 1, str_locate_all(dot_node, '\\"')[[1]][2,2] - 1))
  }
  antecedent <- vector()
  for (i in seq(1, length(aux), 2)){
    antecedent <- c(antecedent, paste(aux[i],aux[i+1]))
  }
  for (node_or_edge in path){
    if (grepl('->',node_or_edge)){
      NEW_EDGES[node_or_edge] <<- dict[node_or_edge]
    } else {
      NEW_NODES[node_or_edge] <<- dict[node_or_edge]
    }
  }
  return(paste0('Si ', paste(antecedent, collapse=' y '), ' entonces ', consequent, 
                ' [Conf:', round(confidence*100, 2), '% Supp:', round(support*100, 2), '%]'))
}

rules2table <- function(rules){
  n <- length(rules)
  antecedents <- vector()
  consequents <- vector()
  confidences <- vector()
  supports <- vector()
  for (rule in rules){
    antecedent <- gsub("Si (.*) entonces (.*) \\((.*)\\) \\[Conf:(.*)% Supp:(.*)%\\]", "\\1", rule)
    antecedents <- c(antecedents, antecedent)
    consequent <- gsub("Si (.*) entonces (.*) \\((.*)\\) \\[Conf:(.*)% Supp:(.*)%\\]", "\\2", rule)
    consequents <- c(consequents, consequent)
    confidence <- gsub("Si (.*) entonces (.*) \\((.*)\\) \\[Conf:(.*)% Supp:(.*)%\\]", "\\4", rule)
    confidences <- c(confidences, as.numeric(confidence))
    support <- gsub("Si (.*) entonces (.*) \\((.*)\\) \\[Conf:(.*)% Supp:(.*)%\\]", "\\5", rule)
    supports <- c(supports, as.numeric(support))
  }
  the_table <- data.frame(A=antecedents, B=consequents, C=confidences, D=supports)
  the_table <- the_table[order(the_table[,2], -the_table[,3]), ]
  colnames(the_table) <- c('Antecedente', 'Consecuente', 'Confianza (%)', 'Soporte (%)')
  rownames(the_table) <- seq(1,n)
  return(the_table)
}

bestTree <- function(formula, data, mm, cc, CONFIDENCE, SUPPORT, output){
  NEW_EDGES <<- vector(mode='list')
  NEW_NODES <<- vector(mode='list')
  RULES <- vector()
  NRECORDS <<- nrow(data)
  model <- J48(formula, data=data, na.action=NULL, control=Weka_control(M=mm,C=cc))

  dot <- capture.output(write_to_dot(model))
  dot <- paste(dot, collapse='\n')
  dot <- str_sub(dot, str_locate(dot, '[{]')[1] + 2, str_locate(dot, '[}]')[1] - 2)
  dot <- str_split(dot, '\n')[[1]]
  nodes <- vector()
  edges <- vector()
  for (d in dot){
    x <- str_split(d, ' [[]')[[1]][1]
    if (grepl('->', x)){
      edges <- c(edges, x)  
    } else {
      nodes <- c(nodes, x)
    }
  }
  n <- length(nodes)
  dict <<- vector(mode="list", length=n)
  names(dict) <<- nodes
  for (d in dot){
    key <- str_split(d, '[ ]', 2)[[1]][1]
    dict[key] <<- d
  }
  m <- matrix(rep(0, n * n), nrow=n)
  rownames(m) <- nodes
  colnames(m) <- nodes
  for (edge in edges){
    i <- str_split(edge, '->')[[1]][1]
    j <- str_split(edge, '->')[[1]][2]
    m[i, j] <- 1
  }
  g <- graph.adjacency(m)
  deg <- degree(g, mode='out')
  paths <- get.shortest.paths(g, 'N0', nodes[deg == 0])$vpath
  for (path in paths){
    rule <- getRule(nodes[path])
    if (rule != ''){
      RULES <- c(RULES,rule)
    }
  }
  new_nodes <- paste(NEW_NODES, collapse='\n')
  new_edges <- paste(NEW_EDGES, collapse='\n')
  new_trees <- paste('digraph J48Tree {\n', new_nodes, '\n', new_edges, '\n}')
  write(new_trees, output)
  return(RULES)
}