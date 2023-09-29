package main

import (
	"fmt"
	"math/rand"
	"strconv"
	"time"
)

type Monk struct {
	monastery  string
	chi_energy int
	number     int
}

func fight(monks []Monk, winner chan Monk) {
	if len(monks) == 2 {
		fmt.Println("Битва бійців №" + strconv.Itoa(monks[0].number) +
			" із енергією Ци " + strconv.Itoa(monks[0].chi_energy) +
			" і №" + strconv.Itoa(monks[1].number) +
			" із енергією Ци " + strconv.Itoa(monks[1].chi_energy))
		time.Sleep(1 * time.Second)
		if monks[0].chi_energy >= monks[1].chi_energy {
			fmt.Println("Боєць №" + strconv.Itoa(monks[0].number) +
				" переміг бійця №" + strconv.Itoa(monks[1].number))
			winner <- monks[0]
		} else {
			fmt.Println("Боєць №" + strconv.Itoa(monks[1].number) +
				" переміг бійця №" + strconv.Itoa(monks[0].number))
			winner <- monks[1]
		}
		return
	} else if len(monks) == 1 {
		fmt.Println("У бійця №" + strconv.Itoa(monks[0].number) +
			" немає суперника. Він проходить у наступний тур автоматично")
		winner <- monks[0]
		return
	}

	//Канали, за якими отримуються ченці, що перемогли у попередньому турі
	monkwin1 := make(chan Monk, 1)
	monkwin2 := make(chan Monk, 1)

	size := int(len(monks) / 2)
	var fighters int
	if len(monks)%2 == 1 {
		fighters = int((len(monks) + 1) / 2)
	} else {
		if size%2 == 1 {
			fighters = size + 1
		} else {
			fighters = size
		}
	}
	go fight(monks[0:fighters], monkwin1)
	go fight(monks[fighters:], monkwin2)
	monk1 := <-monkwin1
	monk2 := <-monkwin2

	fmt.Println("Битва бійців №" + strconv.Itoa(monk1.number) +
		" із енергією Ци " + strconv.Itoa(monk1.chi_energy) +
		" і №" + strconv.Itoa(monk2.number) +
		" із енергією Ци " + strconv.Itoa(monk2.chi_energy))
	time.Sleep(1 * time.Second)
	if monk1.chi_energy >= monk2.chi_energy {
		fmt.Println("Боєць №" + strconv.Itoa(monk1.number) +
			" переміг бійця №" + strconv.Itoa(monk2.number))
		winner <- monk1
	} else {
		fmt.Println("Боєць №" + strconv.Itoa(monk2.number) +
			" переміг бійця №" + strconv.Itoa(monk1.number))
		winner <- monk2
	}
}

func tournament(monkFighters []Monk) {
	tWinChan := make(chan Monk, 1)
	fight(monkFighters, tWinChan)
	tournamentWinner := <-tWinChan
	fmt.Println("У турнірі переміг боєць №" + strconv.Itoa(tournamentWinner.number) +
		" із монастиру " + tournamentWinner.monastery + "! Його монастир забирає статує боддісатви!")
}

func main() {
	// fmt.Print("Скільки турів має бути у турнірі?: ")
	// var tours int
	// fmt.Scan(&tours)

	// participants := 0
	// participants += int(math.Pow(2, float64(tours)))
	// fmt.Printf("Кількість учасників становить %d", participants)

	fmt.Print("Скільки учасників має бути у турнірі?: ")
	var participants int
	fmt.Scan(&participants)

	monkFighters := make([]Monk, 0)

	//Створення масиву ченців
	for i := 0; i < participants; i++ {
		var addMonk Monk
		if i%2 == 0 {
			addMonk = Monk{
				monastery:  "Гуань-Інь",
				chi_energy: rand.Intn(participants),
				number:     (i + 1),
			}
		} else {
			addMonk = Monk{
				monastery:  "Гуань-Янь",
				chi_energy: rand.Intn(participants),
				number:     (i + 1),
			}
		}
		monkFighters = append(monkFighters, addMonk)
	}
	tournament(monkFighters)
}
