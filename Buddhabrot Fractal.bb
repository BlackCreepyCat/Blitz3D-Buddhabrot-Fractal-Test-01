Graphics 800, 600, 32, 2 ; Résolution 800x600, mode fenêtre
; ----------------------------------------
; Name : Buddhabrot By Filax/CreepyCat
; Date : (C)2025 
; Site : https://github.com/BlackCreepyCat
; ----------------------------------------

SetBuffer BackBuffer()

; Paramètres de la fractale
Global imgWidth = 800
Global imgHeight = 600
Global maxIter = 200 ; Nombre max d'itérations par point5
Global samples = 5000000 ; Nombre de points aléatoires à tester

; Créer une image pour stocker les densités
Global buddhaImage = CreateImage(imgWidth, imgHeight)
Dim density#(imgWidth - 1, imgHeight - 1) ; Tableau pour stocker les densités (indices de 0 à n-1)
Dim trajectoryX#(maxIter - 1) ; Dimensionner correctement (indices de 0 à maxIter-1)
Dim trajectoryY#(maxIter - 1)

; Fonction pour calculer une trajectoire et remplir la densité
Function ComputeBuddhabrot()
    For i = 1 To samples
        ; Générer un point aléatoire dans le plan complexe
        Local cr# = Rnd(-2.0, 1.0) ; Partie réelle
        Local ci# = Rnd(-1.5, 1.5) ; Partie imaginaire
        
        Local zr# = 0, zi# = 0 ; z initial = 0
        Local trajectoryLength = 0
        
        ; Calculer la trajectoire
        For n = 0 To maxIter - 1
            Local zr2# = zr * zr
            Local zi2# = zi * zi
            If zr2 + zi2 > 4 ; Condition d'échappement (|z| > 2)
                ; Stocker la trajectoire pour ce point qui échappe
                For t = 0 To trajectoryLength - 1
                    Local x = trajectoryX(t)
                    Local y = trajectoryY(t)
                    If x >= 0 And x < imgWidth And y >= 0 And y < imgHeight
                        density(x, y) = density(x, y) + 1
                    EndIf
                Next
                Exit
            EndIf
            
            ; Mettre à jour z = z^2 + c
            zi = 2 * zr * zi + ci
            zr = zr2 - zi2 + cr
            
            ; Convertir les coordonnées complexes en pixels
            trajectoryX(trajectoryLength) = ScaleToPixelX(zr)
            trajectoryY(trajectoryLength) = ScaleToPixelY(zi)
            trajectoryLength = trajectoryLength + 1
        Next
    Next
End Function

; Convertir coordonnées complexes en pixels
Function ScaleToPixelX#(zr#)
    Return (zr + 2) * imgWidth / 3 ; Ajuste [-2, 1] à [0, imgWidth]
End Function

Function ScaleToPixelY#(zi#)
    Return (zi + 1.5) * imgHeight / 3 ; Ajuste [-1.5, 1.5] à [0, imgHeight]
End Function

Function RenderBuddhabrot()
    LockBuffer ImageBuffer(buddhaImage)
    Local maxDensity# = 0
    
    ; Trouver la densité maximale pour normaliser
    For x = 0 To imgWidth - 1
        For y = 0 To imgHeight - 1
            If density(x, y) > maxDensity Then maxDensity = density(x, y)
        Next
    Next
    
    ; Dessiner les pixels avec un dégradé de couleurs
    For x = 0 To imgWidth - 1
        For y = 0 To imgHeight - 1
            Local value# = 0
            If maxDensity > 0 Then value = density(x, y) / maxDensity * 255
            
            ; Calculer les composantes RGB pour un dégradé
            Local r#, g#, b#
            If value < 85 Then
                ; Noir (0) à Bleu (85)
                r = 0
                g = 0
                b = value * 3 ; Bleu augmente de 0 à 255
            ElseIf value < 170 Then
                ; Bleu (85) à Vert (170)
                r = 0
                g = (value - 85) * 3 ; Vert augmente de 0 à 255
                b = 255 - (value - 85) * 3 ; Bleu diminue de 255 à 0
            Else
                ; Vert (170) à Rouge (255)
                r = (value - 170) * 3 ; Rouge augmente de 0 à 255
                g = 255 - (value - 170) * 3 ; Vert diminue de 255 à 0
                b = 0
            EndIf
            
            ; Assurer que les valeurs restent dans [0, 255]
            If r > 255 Then r = 255
            If g > 255 Then g = 255
            If b > 255 Then b = 255
            
            ; Combiner les composantes RGB en une valeur 32 bits
            Local colorValue = (r Shl 16) Or (g Shl 8) Or b
            WritePixelFast x, y, colorValue, ImageBuffer(buddhaImage)
        Next
    Next
    
    UnlockBuffer ImageBuffer(buddhaImage)
End Function

; Boucle principale
ComputeBuddhabrot() ; Calculer la fractale
RenderBuddhabrot()  ; Rendre l'image

Repeat
    Cls
    DrawImage buddhaImage, 0, 0 ; Afficher l'image
    Flip
Until KeyHit(1) ; Quitter avec Échap

; Libérer la mémoire
FreeImage buddhaImage
End