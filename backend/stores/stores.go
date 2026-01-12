package stores

import "dawpitech/area/models"

var ActionStore = make(map[string]models.Action)
var ReactionStore = make(map[string]models.Reaction)
var ModifierStore = make(map[string]models.Modifier)

var ActionList []models.Action
var ReactionList []models.Reaction
var ModifierList []models.Modifier
