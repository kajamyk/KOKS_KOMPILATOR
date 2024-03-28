# JFK_MBKG
Repozytorium projektu 2 w ramach przedmiotu Języki formalne i kompilatory, wykonywanego przez Karolinę Gałczyńską i Michała Balasa. Do wykonania projektu został wykorzystany język Java oraz generator ANTLR.

## Podręcznik użytkowania
Język pozwala na tworzenie prostych programów z rozszerzeniem _.mbkg_. Umożliwia tworzenie zmiennych dwóch typów (integer oraz float), o różnych zakresach (lokalny i globalny) oraz wykonywanie na nich podstawowych operacji arytmetycznych (dodawanie, odejmowanie, mnożenie, dzielenie). Obsługuje również zmienne tablicowe o statycznej długości. Pozwala na obsługę standardowego wejścia-wyjścia (funkcje _print_ i _scan_) oraz definiowanie funkcji przez użytkownika (z ich późniejszą obsługą). Zezwala również na sterowanie przepływem danych w programie za pomocą podstawowych pętli i instrukcji warunkowych (if-else).

### Zmienne
Typy zmiennych:
- **int** - typ całkowity
- **float** - typ zmiennoprzecinkowy (Float64)

Obsługiwane jest wyłącznie statyczne typowanie zmiennych, zgodność typów zmiennych weryfikowana jest podczas analizy leksykalno-składniowej.

Opearcje na zmiennych:
- deklaracja
- deklaracja z przypisaniem
- przypisanie

```
int x
int y = 5
x = 10

float a
float b = 1.23
a = 4.56
```

Operacje arytmetyczne na zmiennych:
- **\+** - dodawanie
- **\-** - odejmowanie
- **\*** - mnożenie
- **/** - dzielenie

```
int x = 2 + 5
int y = 6 - x

float a = 1.5 * 7
float b = a / 3
```

Operacje warunkowe:
- **==** - równość
- **!=** - nierówność
- **>=** - większe lub równe
- **<=** - mniejsze lub równe
- **>** - większe
- **<** - mniejsze

Operacje warunkowe obsługują porównywanie wartości dwóch stałych, zmiennej i stałej lub dwóch zmiennych.

### Zmienne tablicowe
Obsługiwane są zmienne tablicowe dla obu podstawowych typów danych obsługiwanych przez język. Tablice o statycznej długości muszą być zadeklarowane razem z zainicjalizowanymi wartościami, których liczba jest zgodna z podaną długością. Wartości elementów tablicy mogą być nadpisywane i odczytywane. Elementy tablicy indeksowane są od 0.

```
int{3} x = [1, 2, 3]
x[0] = 5
print(x[1])

float{3} y = [1.23, 4.56, 7.89]
y[2] = 0.01
print(y[0])
```

### Funkcje wbudowane
- scan - obsługuje standardowe wejście; przyjmuje wartości wpisywane przez użytkownika do uprzednio zadeklarowanych zmiennych
- print - obsługuje standardowe wyjście; wyświetla wartości uprzednio zadeklarowanych zmiennych (o ile posiadają one przypisaną wartość) oraz stałych

```
int x
scan(x)
print(x)

float y = 1.23
scan(y)
print(y)

print(5)
print(0.02)
```

### Funkcje definiowane przez użytkownika
Składnia funkcji:
```
<typ zwracany> function <nazwa funkcji> (<typ argumentu> <argument>, ...) begin
    <blok funkcji>
    return <zwracana zmienna>
endfunction
```
Funkcje wymagają podania typu zwracanej wartości oraz typów argumentów. Przy wywoływaniu funkcji argumentami mogą być zmienne zadeklarowane uprzednio w programie lub stałe. Zmienne zadeklarowane wewnątrz funkcji traktowane są jako zmienne lokalne i tracą referencję w momencie wyjścia z funkcji. Funkcje nie mogą być zagnieżdżone.
```
float function square(float num) begin
    print(num)
    float res
    res = num * num 
    return res
endfunction
```

### Instrukcje warunkowe
```
if <warunek> begin
<zachowanie dla prawdziwego warunku>
endif else
<zachowanie dla fałszywego warunku>
endelse
```
Jako warunek wykonania bloku instrukcji warunkowej zastosowana może być każda z obsługiwanych przez język operacji warunkowych. Argumentami operacji mogą być stałe lub zmienne zadeklarowane uprzednio w programie. Instrukcje warunkowe mogą być zagnieżdżone.
```
int y = 2
if y == 2 begin
    int z = y * 10
    print(z)
endif
else
    print(y)
endelse
```

### Pętle
```
loop <warunek> begin
    <blok pętli>
endloop
```
Pętle wymagają podania zadeklarowanej wcześniej zmiennej, której wartość jest modyfikowana przed każdym przejściem pętli (zwiększana o 1). Zmienna musi być typu całkowitego. Zmienne zadeklarowane wewnątrz funkcji traktowane są jako zmienne lokalne i tracą referencję w momencie wyjścia z pętli. Jako warunek zakończenia wykonywania pętli zastosowana może być każda z obsługiwanych przez język operacji warunkowych. Pętle mogą być zagnieżdżone.
```
int x = 0
loop x < 5 begin
    print(x)
endloop
```

