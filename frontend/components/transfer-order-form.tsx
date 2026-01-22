"use client"

import { useState, useEffect, useRef } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { authService } from "@/lib/services/auth.service"
import { keyService } from "@/lib/services/key.service"
import { transactionService } from "@/lib/services/transaction.service"
import type { UserDto } from "@/lib/types/auth"
import type { UserKeys } from "@/lib/types/key"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Separator } from "@/components/ui/separator"
import { Badge } from "@/components/ui/badge"
import {
  Shield,
  Video,
  Send,
  AlertTriangle,
  CheckCircle2,
  Euro,
  Building2,
  FileText,
  Lock,
  Clock,
  Fingerprint,
} from "lucide-react"

export function TransferOrderForm() {
  const router = useRouter()
  const [step, setStep] = useState(1)
  const [amount, setAmount] = useState("")
  const [validity, setValidity] = useState<string>("12")
  const [selectedKeyId, setSelectedKeyId] = useState<string>("")
  const [isRecording, setIsRecording] = useState(false)
  const [otherUsers, setOtherUsers] = useState<UserDto[]>([])
  const [selectedUserId, setSelectedUserId] = useState("")
  const [isLoadingUsers, setIsLoadingUsers] = useState(true)
  const [validKeys, setValidKeys] = useState<UserKeys[]>([])
  const [isLoadingKeys, setIsLoadingKeys] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  
  // Références pour la webcam et l'enregistrement vidéo
  const videoRef = useRef<HTMLVideoElement>(null)
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const [recordedVideo, setRecordedVideo] = useState<string | null>(null)
  const [recordedVideoBlob, setRecordedVideoBlob] = useState<Blob | null>(null)
  const [recordingTime, setRecordingTime] = useState(0)

  useEffect(() => {
    const fetchOtherUsers = async () => {
      try {
        setIsLoadingUsers(true)
        const users = await authService.getOtherUsers()
        setOtherUsers(users)
      } catch (error) {
        console.error("Erreur lors du chargement des utilisateurs:", error)
      } finally {
        setIsLoadingUsers(false)
      }
    }

    fetchOtherUsers()
  }, [])

  // Charger les clés valides quand on arrive à l'étape 3
  useEffect(() => {
    if (step === 3) {
      const fetchValidKeys = async () => {
        try {
          setIsLoadingKeys(true)
          const keys = await keyService.listValidKeys()
          setValidKeys(keys)
        } catch (error) {
          console.error("Erreur lors du chargement des clés valides:", error)
        } finally {
          setIsLoadingKeys(false)
        }
      }

      fetchValidKeys()
    }
  }, [step])

  // Gérer l'enregistrement vidéo
  useEffect(() => {
    let interval: NodeJS.Timeout | null = null

    if (isRecording) {
      interval = setInterval(() => {
        setRecordingTime((prev) => {
          if (prev >= 30) {
            // Arrêter l'enregistrement automatiquement après 30 secondes
            if (mediaRecorderRef.current && isRecording) {
              mediaRecorderRef.current.stop()
              setIsRecording(false)
            }
            if (streamRef.current) {
              streamRef.current.getTracks().forEach((track) => track.stop())
              streamRef.current = null
            }
            if (videoRef.current) {
              videoRef.current.srcObject = null
            }
            return 30
          }
          return prev + 1
        })
      }, 1000)
    } else {
      setRecordingTime(0)
    }

    return () => {
      if (interval) clearInterval(interval)
    }
  }, [isRecording])

  // Nettoyer le stream quand le composant se démonte ou change d'étape
  useEffect(() => {
    return () => {
      if (streamRef.current) {
        streamRef.current.getTracks().forEach((track) => track.stop())
      }
    }
  }, [])

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true,
      })

      if (videoRef.current) {
        videoRef.current.srcObject = stream
        videoRef.current.play()
      }

      streamRef.current = stream

      const mediaRecorder = new MediaRecorder(stream, {
        mimeType: "video/webm",
      })

      const chunks: Blob[] = []

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunks.push(event.data)
        }
      }

      mediaRecorder.onstop = () => {
        const blob = new Blob(chunks, { type: "video/webm" })
        const videoUrl = URL.createObjectURL(blob)
        setRecordedVideo(videoUrl)
        setRecordedVideoBlob(blob)
      }

      mediaRecorderRef.current = mediaRecorder
      mediaRecorder.start()
      setIsRecording(true)
    } catch (error) {
      console.error("Erreur lors de l'accès à la webcam:", error)
      alert("Impossible d'accéder à la webcam. Veuillez vérifier les permissions.")
    }
  }

  const stopRecording = () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop()
      setIsRecording(false)
    }

    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop())
      streamRef.current = null
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null
    }
  }

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`
  }

  const formatAmount = (value: string) => {
    const num = value.replace(/\D/g, "")
    if (num) {
      return new Intl.NumberFormat("fr-FR").format(Number.parseInt(num))
    }
    return ""
  }

  const handleSubmitTransaction = async () => {
    if (!selectedKeyId || !recordedVideoBlob || !selectedUserId || !amount) {
      setError("Veuillez remplir tous les champs requis")
      return
    }

    setIsSubmitting(true)
    setError(null)

    try {
      // Trouver l'utilisateur sélectionné pour obtenir son ID
      const selectedUser = otherUsers.find((user) => user.userId.toString() === selectedUserId)
      if (!selectedUser || !selectedUser.userId) {
        throw new Error("Impossible de trouver l'ID de l'utilisateur sélectionné")
      }

      // Trouver la clé sélectionnée pour obtenir la publicKey
      const selectedKey = validKeys.find((key) => key.id.toString() === selectedKeyId)
      if (!selectedKey) {
        throw new Error("Impossible de trouver la clé sélectionnée")
      }

      // Convertir le montant en nombre (enlever les espaces de formatage)
      const amountNumber = parseFloat(amount.replace(/\s/g, "").replace(",", "."))

      // Convertir le blob en File
      const videoFile = new File([recordedVideoBlob], "video.webm", { type: "video/webm" })

      // Appeler le service de transaction
      await transactionService.createTransaction(
        selectedUser.userId,
        amountNumber,
        parseInt(validity),
        parseInt(selectedKeyId),
        selectedKey.publicKey,
        videoFile
      )

      setSuccess(true)
      
      // Réinitialiser le formulaire après 2 secondes et rediriger vers le dashboard
      setTimeout(() => {
        setStep(1)
        setAmount("")
        setSelectedUserId("")
        setValidity("12")
        setSelectedKeyId("")
        setRecordedVideo(null)
        setRecordedVideoBlob(null)
        setRecordingTime(0)
        setSuccess(false)
        router.push("/dashboard")
      }, 2000)
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || "Erreur lors de la création de la transaction"
      setError(typeof errorMessage === "string" ? errorMessage : "Erreur de création")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
      {/* Formulaire principal */}
      <div className="xl:col-span-2 space-y-6">
        {/* Étapes */}
        <div className="flex items-center gap-4">
          {[
            { num: 1, label: "Informations" },
            { num: 2, label: "Validation vidéo" },
            { num: 3, label: "Insertion clé privé" },
          ].map((s, idx) => (
            <div key={s.num} className="flex items-center gap-3">
              <div
                className={`flex items-center justify-center w-8 h-8 rounded-full text-sm font-medium ${
                  step >= s.num ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"
                }`}
              >
                {step > s.num ? <CheckCircle2 className="w-4 h-4" /> : s.num}
              </div>
              <span className={`text-sm font-medium ${step >= s.num ? "text-foreground" : "text-muted-foreground"}`}>
                {s.label}
              </span>
              {idx < 2 && <div className="w-12 h-px bg-border" />}
            </div>
          ))}
        </div>

        {/* Étape 1 - Informations du virement */}
        {step === 1 && (
          <Card className="bg-card border-border">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-card-foreground">
                Informations du virement
              </CardTitle>
              <CardDescription>Saisissez les détails de l'ordre de virement</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="amount">Montant :</Label>
                  <div className="relative">
                    <Input
                      id="amount"
                      placeholder="0"
                      value={amount}
                      onChange={(e) => setAmount(formatAmount(e.target.value))}
                      className="pr-12 text-lg font-semibold bg-input border-border"
                    />
                    <span className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground font-medium">
                      RS
                    </span>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="debit-account">Donner ordre à :</Label>
                  <Select value={selectedUserId} onValueChange={setSelectedUserId} disabled={isLoadingUsers}>
                    <SelectTrigger className="bg-input border-border" id="debit-account">
                      <SelectValue placeholder={isLoadingUsers ? "Chargement..." : "Sélectionner un compte"} />
                    </SelectTrigger>
                    <SelectContent>
                      {otherUsers.length === 0 && !isLoadingUsers ? (
                        <SelectItem value="" disabled>Aucun utilisateur disponible</SelectItem>
                      ) : (
                        otherUsers.map((user) => (
                          <SelectItem key={user.userId.toString()} value={user.userId.toString() || ""}>
                            {user.userName}
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="validity">Validité (en mois)</Label>
                  <Select value={validity} onValueChange={setValidity}>
                    <SelectTrigger className="bg-input border-border" id="validity">
                      <SelectValue placeholder="Sélectionner une durée" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="1">1 mois</SelectItem>
                      <SelectItem value="3">3 mois</SelectItem>
                      <SelectItem value="6">6 mois</SelectItem>
                      <SelectItem value="12">12 mois</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="flex justify-between">
                <Button variant="outline" onClick={() => setStep(1)} className="gap-2">
                  Retour
                </Button>
                <Button onClick={() => setStep(2)} className="gap-2">
                  Continuer vers la validation
                  <Video className="w-4 h-4" />
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Étape 2 - Validation vidéo */}
        {step === 2 && (
          <Card className="bg-card border-border">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-card-foreground">
                <Video className="w-5 h-5 text-primary" />
                Authentification vidéo
              </CardTitle>
              <CardDescription>Enregistrez une confirmation vidéo pour valider l'ordre de virement</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Zone vidéo */}
              <div className="relative aspect-video bg-secondary rounded-lg overflow-hidden border border-border">
                {recordedVideo && !isRecording ? (
                  <video
                    src={recordedVideo}
                    controls
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <>
                    <video
                      ref={videoRef}
                      autoPlay
                      muted
                      playsInline
                      className={`w-full h-full object-cover ${!isRecording ? "hidden" : ""}`}
                    />
                    {!isRecording && (
                      <div className="absolute inset-0 flex flex-col items-center justify-center gap-4">
                        <div className="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center">
                          <Video className="w-10 h-10 text-primary" />
                        </div>
                        <p className="text-muted-foreground text-center max-w-md">
                          Cliquez sur "Démarrer l'enregistrement" pour confirmer vocalement votre ordre de virement
                        </p>
                      </div>
                    )}
                    {isRecording && (
                      <div className="absolute inset-0 flex flex-col items-center justify-center gap-2 pointer-events-none">
                        <div className="w-16 h-16 rounded-full bg-destructive/20 flex items-center justify-center animate-pulse">
                          <div className="w-4 h-4 rounded-full bg-destructive" />
                        </div>
                        <p className="text-foreground font-medium bg-card/80 px-3 py-1 rounded">Enregistrement en cours...</p>
                        <p className="text-foreground text-sm font-mono bg-card/80 px-3 py-1 rounded">{formatTime(recordingTime)} / 00:30</p>
                      </div>
                    )}
                  </>
                )}

                {/* Indicateurs sécurité */}
                <div className="absolute top-4 left-4 flex items-center gap-2 z-10">
                  <Badge variant="outline" className="bg-card/80 backdrop-blur gap-1.5">
                    <Lock className="w-3 h-3" />
                    SHA-256
                  </Badge>
                </div>
                <div className="absolute top-4 right-4 flex items-center gap-2 z-10">
                  <Badge variant="outline" className="bg-card/80 backdrop-blur gap-1.5">
                    <Shield className="w-3 h-3 text-primary" />
                    RSA
                  </Badge>
                </div>
              </div>

              {/* Boutons */}
              <div className="flex flex-col sm:flex-row gap-3">
                {recordedVideo && !isRecording ? (
                  <Button
                    variant="outline"
                    className="flex-1 gap-2"
                    onClick={() => {
                      setRecordedVideo(null)
                      setRecordedVideoBlob(null)
                      setRecordingTime(0)
                    }}
                  >
                    Réenregistrer
                  </Button>
                ) : (
                  <Button
                    variant={isRecording ? "destructive" : "default"}
                    className="flex-1 gap-2"
                    onClick={() => {
                      if (isRecording) {
                        stopRecording()
                      } else {
                        startRecording()
                      }
                    }}
                  >
                    {isRecording ? (
                      <>
                        <div className="w-3 h-3 rounded-full bg-white" />
                        Arrêter l'enregistrement
                      </>
                    ) : (
                      <>
                        <Video className="w-4 h-4" />
                        Démarrer l'enregistrement
                      </>
                    )}
                  </Button>
                )}
              </div>

              <Separator className="bg-border" />

              <div className="flex justify-between">
                <Button variant="outline" onClick={() => setStep(1)} className="gap-2">
                  Retour
                </Button>
                <Button className="gap-2" disabled={!recordedVideo} onClick={() => setStep(3)}> 
                  <Send className="w-4 h-4" />
                  Continuer vers l'insertion clé privé
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Étape 3 - Insertion clé privé */}
        {step === 3 && (
          <Card className="bg-card border-border">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-card-foreground">
                Choix de la clé à utiliser
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="key-select">Choisir clé</Label>
                  <Select value={selectedKeyId} onValueChange={setSelectedKeyId} disabled={isLoadingKeys}>
                    <SelectTrigger className="bg-input border-border" id="key-select">
                      <SelectValue placeholder={isLoadingKeys ? "Chargement..." : "Sélectionner une clé"} />
                    </SelectTrigger>
                    <SelectContent>
                      {validKeys.length === 0 && !isLoadingKeys ? (
                        <SelectItem value="" disabled>Aucune clé valide disponible</SelectItem>
                      ) : (
                        validKeys.map((key) => (
                          <SelectItem key={key.id} value={key.id.toString()}>
                            {key.keyName}
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>
                  {selectedKeyId && (
                    <p className="text-xs text-muted-foreground mt-1">
                      Clé sélectionnée : {validKeys.find((k) => k.id.toString() === selectedKeyId)?.keyName}
                    </p>
                  )}
                </div>
              </div> 

              {error && (
                <Alert variant="destructive">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              {success && (
                <Alert className="bg-emerald-500/10 border-emerald-500/20">
                  <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                  <AlertDescription className="text-emerald-700 dark:text-emerald-400">
                    Transaction créée avec succès !
                  </AlertDescription>
                </Alert>
              )}

              <div className="flex justify-between">
                <Button variant="outline" onClick={() => setStep(2)} className="gap-2" disabled={isSubmitting}>
                  Retour
                </Button>
                <Button 
                  className="gap-2" 
                  disabled={!selectedKeyId || !recordedVideoBlob || isSubmitting || success}
                  onClick={handleSubmitTransaction}
                >
                  {isSubmitting ? "Enregistrement..." : "Valider"}
                </Button>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
